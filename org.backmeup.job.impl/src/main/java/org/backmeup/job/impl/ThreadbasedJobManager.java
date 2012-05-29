package org.backmeup.job.impl;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.job.JobManager;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileEntry;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;

/**
 * The ThreadbasedJobManager creates a thread which 
 * will periodically checks for a new backup job, 
 * executing it and printing all errors to stderr.
 * 
 * It will be replaced by a robust high scaling version.
 * 
 * @author fschoeppl
 *
 */
@ApplicationScoped
public class ThreadbasedJobManager implements JobManager {

	public class ConsoleProgressor implements Progressable {
		public void progress(String message) {
			System.out.println(message);
		}
	} 
	
	private ThreadJobExecutor je;
	private List<BackupJob> jobs;
	@Inject
	private Plugin plugins;
	private int maxid; 
	@Inject
  @Named("job.temporaryDirectory")
  private String temporaryDirectory;
	
	public ThreadbasedJobManager() {
		this.jobs = Collections.synchronizedList(new ArrayList<BackupJob>());
		temporaryDirectory = "temp";
	}
	
	public BackupJob createBackupJob(User user,
			List<ProfileOptions> sourceProfiles, Profile sinkProfile,
			List<ActionDescribable> requiredActions,
			String timeExpression, String keyRing) {
		BackupJob bj = new BackupJob(maxid++, user, sourceProfiles, sinkProfile, requiredActions, timeExpression);
		jobs.add(bj);		
		return bj;
	}
 
	public class ThreadJobExecutor extends Thread {
		private volatile boolean running;
		
		
		public ThreadJobExecutor() {
			running = true;
			setDaemon(false);
		}
		
		private Properties convertToProperties(Profile p) {
			Properties props = new Properties();
			for (ProfileEntry pe : p.getEntries()) {
				props.setProperty(pe.getKey(), pe.getValue());
			}
			return props;
		}
		
		public void run() {
		  System.err.println("ThreadBasedExecutor: " + Thread.currentThread().getName());
  		if (temporaryDirectory == null) {
  		  throw new IllegalStateException("A temporary folder must be specified within bl.properties: temporaryDirectory = somefolder");
  		}
  		temporaryDirectory = temporaryDirectory + "/cache";
			while (isRunning()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) { 
					e.printStackTrace();
				}
				if (!jobs.isEmpty()) {
					BackupJob job = jobs.get(0);
					jobs.remove(0);
					
					
					Datasink sink = plugins.getDatasink(job.getSinkProfile().getDesc());
					Properties sinkProps = convertToProperties(job.getSinkProfile());
					for (ProfileOptions po : job.getSourceProfiles()) {
						Datasource source = plugins.getDatasource(po.getProfile().getDesc());
						Properties sourceProperties = convertToProperties(po.getProfile());						
						try {
							StorageWriter writer = new LocalFilesystemStorageWriter();
							StorageReader reader = new LocalFilesystemStorageReader();
							try {
							  DateFormat format = new SimpleDateFormat("yyyy_MM_dd hh_mm");
							  String time = format.format(new Date());
								File f = new File(temporaryDirectory + "/" + po.getProfile().getProfileName() + "_" + time);
								f.mkdirs();
								writer.open(f.getPath());
								reader.open(f.getPath());					
							} catch (StorageException e1) {
								e1.printStackTrace();
							}
							source.downloadAll(sourceProperties, writer, new ConsoleProgressor());
							sink.upload(sinkProps, reader, new ConsoleProgressor());
						} catch (DatasourceException e) {
							e.printStackTrace();
						} catch (StorageException e) {
							e.printStackTrace();
						} catch (BackMeUpException me) {
						  me.printStackTrace();
						}
					}					
				}
			}
		}

		public boolean isRunning() {
			return running;
		}

		public void setRunning(boolean running) {
			this.running = running;
		}
	}

	public void shutdown() {
	  System.out.println("Shutting down ThreadbasedJobManager!");
		this.je.setRunning(false);
		try {
		  je.join();
		} catch(InterruptedException ie) {
		  ie.printStackTrace();
		}
	}

	public Plugin getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugin plugins) {
		this.plugins = plugins;
	}

  @Override
  public void start() {
    System.out.println("Starting up ThreadbasedJobManager!");
    je = new ThreadJobExecutor();
    je.start();    
  }  
}
