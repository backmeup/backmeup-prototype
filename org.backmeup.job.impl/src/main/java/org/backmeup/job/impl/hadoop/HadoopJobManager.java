package org.backmeup.job.impl.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.backmeup.job.impl.AkkaJobManager;
import org.backmeup.job.impl.hadoop.HadoopJobRunner;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;

/**
 * An implementation of {@link AkkaJobManager} that pushes backup
 * jobs onto a Hadoop cluster.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
// @ApplicationScoped
public class HadoopJobManager extends AkkaJobManager {
	
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
	protected Runnable newJobRunner(final BackupJob job) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					JobConf jobConf = getMRC().createJobConf();
					jobConf.setJobName("job" + job.getId());
					jobConf.setJarByClass(HadoopJobRunner.class);
					jobConf.setMapRunnerClass(HadoopJobRunner.class);
					jobConf.setSpeculativeExecution(false);
					jobConf.setNumMapTasks(1);
					jobConf.setNumReduceTasks(0);
					jobConf.set("job", JsonSerializer.serialize(job));
					
					// TODO configure via properties
					jobConf.set("pluginsDir", "/home/simonr/Workspaces/backmeup/backmeup-prototype/org.backmeup.embedded/autodeploy");
					jobConf.set("osgiTempDir", "/home/simonr/Workspaces/backmeup/backmeup-prototype/osgi-tmp");
					jobConf.set("indexURI", "http://localhost:9200");

					JobClient.runJob(jobConf);
				} catch (IOException e) {
					// TODO error handling not forseen in the interface?
					throw new RuntimeException(e);
				}
			}
		};
	}

}
