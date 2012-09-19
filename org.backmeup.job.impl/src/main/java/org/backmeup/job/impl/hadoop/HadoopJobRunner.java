package org.backmeup.job.impl.hadoop;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.backmeup.job.impl.BackupJobRunner;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.Plugin;
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
			"org.backmeup.plugin.api " + 
			"org.backmeup.plugin.api.actions " +
			"javax.mail " +
			"com.sun.imap ";
	
	private JobConf conf;
	
	// private String indexURI;
	
	private BackupJob job;
	
	private Plugin plugins;
	
	private Keyserver keyserver;

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
	    
	  //TODO: Exchange with https constructor; use parameters from bl.properties + truststores from plone
	  keyserver = new org.backmeup.keyserver.client.impl.Keyserver(	      
	    "keysrv.backmeup.at",
	    "/keysrv",
	    true
	  );
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

		FileSystem hdfs = FileSystem.get(conf);
		
		StorageWriter storageWriter = new HdfsStorageWriter(hdfs);
		StorageReader storageReader = new HdfsStorageReader(hdfs);

		BackupJobRunner runner = new BackupJobRunner(plugins, keyserver);
		runner.executeBackup(job, storageReader, storageWriter);
			
		plugins.shutdown();
		System.out.println("Backupjob complete.");
	}

}
