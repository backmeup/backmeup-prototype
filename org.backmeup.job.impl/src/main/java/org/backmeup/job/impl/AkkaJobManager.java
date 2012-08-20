package org.backmeup.job.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.job.JobManager;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.backmeup.model.serializer.JsonSerializer;

import akka.actor.ActorSystem;
import akka.util.Duration;

/**
 * A 'JobManager' implementation that supports scheduled execution (backed by
 * the BackendJob entities in the database) using Akka and Hadoop.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@ApplicationScoped
public class AkkaJobManager implements JobManager {
	
	private static final ActorSystem system = ActorSystem.create();
	
	private BackupJobDao backupJobDao = null;
	
	@Inject
	private Connection conn;
	
	@Inject
	private DataAccessLayer dal;
	
	/**
	 * HDFS Distributed filesystem cluster
	 * TODO inject
	 */
	private MiniDFSCluster dfsCluster = null;
	
	/**
	 * Hadoop Map/Reduce cluster
	 * TODO inject
	 */
	private MiniMRCluster mrCluster = null;

	private BackupJobDao getDao() {
		// BackupJobDao lazy creation - TODO inject
		if (backupJobDao == null)
			backupJobDao = dal.createBackupJobDao();
		
		return backupJobDao;
	}
	
	private MiniDFSCluster getHDFS() throws IOException {
		// HDFS cluster lazy creation
		if (dfsCluster == null) {
		    Configuration conf = new Configuration();
		    conf.set("dfs.datanode.data.dir.perm", "775");
		    dfsCluster = new MiniDFSCluster(conf, 1, true, null);
		    dfsCluster.getFileSystem().makeQualified(new Path("input"));
		    dfsCluster.getFileSystem().makeQualified(new Path("output"));
		}
		
		return dfsCluster;
	}
	
	private MiniMRCluster getMRC() throws IOException {
		if (mrCluster == null)
			mrCluster = new MiniMRCluster(1, getHDFS().getFileSystem().getUri().toString(), 1);
		return mrCluster;
	}

	@Override
	public BackupJob createBackupJob(User user,
			Set<ProfileOptions> sourceProfiles, Profile sinkProfile,
			Set<ActionProfile> requiredActions, Date start, long delayInMs,
			String keyRing) {
		
		// Create BackupJob entity in DB...
	    BackupJob job = new BackupJob(
	    		user,
	    		sourceProfiles,
	    		sinkProfile,
	            requiredActions, 
	            start, delayInMs);
	    getDao().save(job);
	    
	    // ... and queue immediately
	    queueJob(job);
	    return job;
	}
	
	public void queueJob(final BackupJob job) {
		try {		    
			// maybe we want to start immediately for the first time, and then add the delay
			long executeIn = job.getStart().getTime() + job.getDelay();  
	    
			system.scheduler().scheduleOnce(
				Duration.create(executeIn, TimeUnit.MILLISECONDS), 
				new Runnable() {
					@Override
					public void run() {
						try {
							JobConf jobConf = getMRC().createJobConf();
							jobConf.setJobName("job" + job.getId());
							jobConf.setJarByClass(HadoopJobRunner.class);
							jobConf.setMapRunnerClass(HadoopJobRunner.class);
							jobConf.setSpeculativeExecution(false);
							jobConf.set("job", JsonSerializer.serialize(job));
							
							// TODO configure via properties
							jobConf.set("indexURI", "http://localhost:9200");
		
							JobClient.runJob(jobConf);
						} catch (IOException e) {
							// TODO error handling not forseen in the interface?
							throw new RuntimeException(e);
						}
					}
				});
		} catch (Exception e) {
			// TODO there must be error handling defined in the JobManager!
			throw new RuntimeException(e);
		}		
	}

	@Override
	public BackupJob getBackUpJob(Long jobId) {
		return getDao().findById(jobId);
	}

	@Override
	public void start() {
		conn.begin();
		
		// TODO only take N next recent ones (at least if allJobs has an excessive length)
		for (BackupJob storedJob : getDao().findAll()) {
			queueJob(storedJob);
		}
	}

	@Override
	public void shutdown() {
		// Do nothing
	}

}
