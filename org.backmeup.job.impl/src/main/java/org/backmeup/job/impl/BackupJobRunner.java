package org.backmeup.job.impl;

import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;

/**
 * Implements the actual BackupJob execution.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at.>
 */
public class BackupJobRunner {
	
	private Plugin plugins;
	
	public BackupJobRunner(Plugin plugins) {
		this.plugins = plugins;
	}
	
	public void executeBackup(BackupJob job, StorageReader storageReader, StorageWriter storageWriter) {
		String tempDir = "job-" + System.currentTimeMillis();
		
		Datasink sink = plugins.getDatasink(job.getSinkProfile().getDesc());
		Properties sinkProperties = job.getSinkProfile().getEntriesAsProperties();
		
        for (ProfileOptions po : job.getSourceProfiles()) {
        	// Download from Source
            System.out.println("Downloading to temporary storage");
        	Datasource source = plugins.getDatasource(po.getProfile().getDesc());
        	Properties sourceProperties = po.getProfile().getEntriesAsProperties();
        	try {
        		storageWriter.open(tempDir);
        		source.downloadAll(sourceProperties, storageWriter, new Progressable() {
					@Override
					public void progress(String message) {
						System.out.println(message);	
					}
				});
        		storageWriter.close();
        	} catch (StorageException e) {
        		// TODO error handling
        		System.out.println("ERROR: " + e.getMessage());
        	} catch (DatasourceException e) {
        		// TODO error handling
        		System.out.println("ERROR: " + e.getMessage());
        	} 
        	System.out.println("Download complete.");
        	
        	// Upload to Sink
        	System.out.println("Uploading to Datasink");      	
        	
        	try {
        		storageReader.open(tempDir);
        		
        		sink.upload(sinkProperties, storageReader, new Progressable() {
					@Override
					public void progress(String message) {
						System.out.println(message);
					}
				});
				
        		storageReader.close();
        	} catch (StorageException e) {
        		// TODO error handling
        		System.out.println("ERROR: " + e.getMessage());       		
        	}
        	System.out.println("Upload complete.");
        }
	}

}
