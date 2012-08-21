package org.backmeup.job.impl;

import java.io.IOException;
import java.util.List;

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
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.storage.StorageWriter;
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
	
	private String indexURI;
	
	private BackupJob job;
	
	private Plugin plugins;

	@Override
	public void configure(JobConf conf) {
		this.indexURI = conf.get("indexURI");
		this.job = JsonSerializer.deserialize(conf.get("job"), BackupJob.class);
		
		this.plugins = new PluginImpl(
				conf.get("pluginsDir"), 
				conf.get("osgiTempDir"), 
				EXPORTED_PACKAGES);
		
		this.plugins.startup();
		((PluginImpl) plugins).waitForInitialStartup();
	}

	@Override
	public void run(RecordReader<Text, BytesWritable> reader, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		
		// TODO workaround for the race condition that seems to occur with OSGi startup
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create a temporary Storage folder on the HDFS to store downloads
		StorageWriter storageWriter = new HDF
		
        for (ProfileOptions po : job.getSourceProfiles()) {
        	Datasource source = plugins.getDatasource(po.getProfile().getDesc());

        }
		
		List<SourceSinkDescribable> sinks = plugins.getConnectedDatasinks();
		System.out.println("sinks: " + sinks.size());
		for (SourceSinkDescribable sink : sinks) {
			System.out.println(sink.getId());
		}
		
		/*
		Text key = reader.createKey();
		BytesWritable value = reader.createValue();
		
		while (reader.next(key, value)) {
			// TODO 
			System.out.println("Processing file " + key);
		}
		*/
		
		plugins.shutdown();
		System.out.println("Backupjob complete.");
	}

}
