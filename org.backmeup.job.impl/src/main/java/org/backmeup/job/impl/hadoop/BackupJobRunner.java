package org.backmeup.job.impl.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

public class BackupJobRunner implements MapRunnable<Text, BytesWritable, Text, Text> {

	@Override
	public void configure(JobConf conf) {
		// Do nothing 
	}

	@Override
	public void run(RecordReader<Text, BytesWritable> reader, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		
		System.out.println("Starting backup job");
		
		Text key = reader.createKey();
		BytesWritable value = reader.createValue();
		
		while (reader.next(key, value)) {
			// TODO 
			System.out.println("Processing file " + key);
		}
		
		System.out.println("Backupjob complete.");
		
	}

}
