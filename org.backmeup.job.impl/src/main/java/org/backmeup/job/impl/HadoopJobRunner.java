package org.backmeup.job.impl;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;

/**
 * This class executes the actual backup Job on the Hadoop cluster.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class HadoopJobRunner implements MapRunnable<Text, BytesWritable, Text, Text> {
	
	private String indexURI;
	
	private BackupJob job;

	@Override
	public void configure(JobConf conf) {
		this.indexURI = conf.get("indexUri");
		this.job = JsonSerializer.deserialize(conf.get("job"), BackupJob.class);
	}

	@Override
	public void run(RecordReader<Text, BytesWritable> reader, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		
		// System.out.println("Starting backup job " + job.getId() + " for user " + job.getUser());
		// System.out.println("Index is at " + indexURI);
		
		Text key = reader.createKey();
		BytesWritable value = reader.createValue();
		
		while (reader.next(key, value)) {
			// TODO 
			System.out.println("Processing file " + key);
		}
		
		System.out.println("Backupjob complete.");
		
	}

}
