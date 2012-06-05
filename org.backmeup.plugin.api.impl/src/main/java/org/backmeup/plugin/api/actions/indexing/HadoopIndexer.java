package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.lucene.index.IndexWriter;

/**
 * A {@link BaseIndexer} subclass that is compatible with Hadoop. Note that this
 * class does not work based on a {@link StorageReader}, but has to work directly
 * on the Hadoop-specific record objects!
 */
public class HadoopIndexer extends BaseIndexer implements MapRunnable<Text, BytesWritable, Text, Text> {

	private JobConf conf;
	
	public void configure(JobConf conf) {
		this.conf = conf;
	}
	
	public void run(RecordReader<Text, BytesWritable> reader, OutputCollector<Text, Text> output,
			Reporter report) throws IOException {
		
		Text key = reader.createKey();
		BytesWritable value = reader.createValue();
		
		IndexWriter indexWriter = createIndexWriter(conf.get("backmeup.index.directory"));
		
		report.setStatus("Adding documents...");
		
		while (reader.next(key, value)) {
			try {
				index(indexWriter, key.toString(), new ByteArrayInputStream(value.getBytes()));
				report.progress();
			} catch (Throwable t) {
				// TODO replace with decent logging
				System.out.println(t.getMessage());
			}
		}

		report.setStatus("Done adding documents.");
		report.setStatus("Closing index...");
		indexWriter.close();
		report.setStatus("Closing done!");
	}

}
