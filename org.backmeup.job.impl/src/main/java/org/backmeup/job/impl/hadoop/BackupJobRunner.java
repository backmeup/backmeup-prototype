package org.backmeup.job.impl.hadoop;

import java.io.IOException;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

public class BackupJobRunner implements MapRunnable {

	private JobConf conf;
	
	@Override
	public void configure(JobConf conf) {
		this.conf = conf;
	}

	@Override
	public void run(RecordReader reader, OutputCollector outputCollector, Reporter reporter)
			throws IOException {
		
		System.out.println("Starting backup job");
		
		Object key = reader.createKey();
		Object value = reader.createValue();
		
		while (reader.next(key, value)) {
			// System.out.println("##### " + key + ": " + value);
		}
		
		System.out.println("Backupjob complete.");
	}

}
