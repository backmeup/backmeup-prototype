package org.backmeup.job.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.job.JobManager;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileEntry;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.DatasinkException;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;

@ApplicationScoped
public class ThreadbasedJobManager implements JobManager {

	public class ConsoleProgressor implements Progressable {
		public void progress(String message) {
			System.out.println(message);
		}
	}
	
	//private DataAccessLayer dal;
	private ThreadJobExecutor je = new ThreadJobExecutor();
	private List<BackupJob> jobs;
	@Inject
	private Plugin plugins;
	private int maxid;
	
	public ThreadbasedJobManager() {
		this.jobs = Collections.synchronizedList(new ArrayList<BackupJob>());
		je.start();
	}
	
	public BackupJob createBackupJob(User user,
			List<ProfileOptions> sourceProfiles, Profile sinkProfile,
			List<ActionDescribable> requiredActions,
			String timeExpression, String keyRing) {
		BackupJob bj = new BackupJob(maxid++, user, sourceProfiles, sinkProfile, requiredActions, timeExpression);
		jobs.add(bj);		
		return bj;
	}
 
	private class ThreadJobExecutor extends Thread {
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
			while (!jobs.isEmpty() || isRunning()) {
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
								File f = new File("C:/Fabian/temp/Cache/"+po.getProfile().getProfileName()+"_"+(new Date()).getTime());
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
						} catch (DatasinkException e) {
							e.printStackTrace();
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
		this.je.setRunning(false);
		while(je.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Plugin getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugin plugins) {
		this.plugins = plugins;
	}  
}
