package org.backmeup.job.impl;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.hdfs.HdfsStorageReader;
import org.backmeup.plugin.api.storage.hdfs.HdfsStorageWriter;
import org.backmeup.plugin.osgi.PluginImpl;

/**
 * This class executes the actual backup Job on the Hadoop cluster.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class HadoopJobRunner implements MapRunnable<Text, BytesWritable, Text, Text> {
	
	private static final String EXPORTED_PACKAGES =
			"org.backmeup.plugin.spi " +
			"org.backmeup.model " +
			"org.backmeup.model.spi " +
			"org.backmeup.plugin.api.connectors " +
			"org.backmeup.plugin.api.storage " +
			"com.google.gson " + 
			"org.backmeup.plugin.api";
	
	private JobConf conf;
	
	// private String indexURI;
	
	private BackupJob job;
	
	private Plugin plugins;

	@Override
	public void configure(JobConf conf) {
		this.conf = conf;
		// this.indexURI = conf.get("indexURI");
		this.job = JsonSerializer.deserialize(conf.get("job"), BackupJob.class);
		
		this.plugins = new PluginImpl(
				conf.get("pluginsDir"), 
				conf.get("osgiTempDir"), 
				EXPORTED_PACKAGES);
		
		this.plugins.startup();
	    ((PluginImpl)plugins).waitForInitialStartup();
	}

	@Override
	public void run(RecordReader<Text, BytesWritable> reader, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		
		// TODO workaround for the race condition that seems to occur with OSGi startup
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* DEBUG: list all available datasources
		List<SourceSinkDescribable> datasources = plugins.getConnectedDatasources();
		System.out.println(datasources.size() + " datasource plugins available");
		for (SourceSinkDescribable source : datasources) {
			System.out.println(source.getId());
		}
		
		// DEBUG: list all available datasinks
		List<SourceSinkDescribable> datasinks = plugins.getConnectedDatasinks();
		System.out.println(datasinks.size() + " datasink plugins available");
		for (SourceSinkDescribable sink : datasinks) {
			System.out.println(sink.getId());
		}
		*/
		
		// Create a temporary storage space on the HDFS
		// TODO replace dummy temp dir naming with decent naming
		String tempDir = "job-" + System.currentTimeMillis();
		FileSystem hdfs = FileSystem.get(conf);
		
		StorageWriter storageWriter = new HdfsStorageWriter(hdfs);
		StorageReader storageReader = new HdfsStorageReader(hdfs);
		
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
		
		plugins.shutdown();
		System.out.println("Backupjob complete.");
	}

}
