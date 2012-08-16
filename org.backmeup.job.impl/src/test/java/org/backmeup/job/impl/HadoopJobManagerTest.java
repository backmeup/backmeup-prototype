package org.backmeup.job.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.log4j.Logger;
import org.backmeup.job.impl.hadoop.BackupJobRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HadoopJobManagerTest {

	private static final String TEST_LOG_DIR = "hadoop-logs";
	private static final String TEST_INPUT_PATH = "src/test/resources";
	private static final String TEST_OUTPUT_PATH = "hadoop-output";

	private MiniDFSCluster dfsCluster = null;
	private MiniMRCluster mrCluster = null;
	
	private final Path input = new Path("input");
	private final Path output = new Path(TEST_OUTPUT_PATH);
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Before
	public void setUp() throws Exception {
		System.out.println("Firing up embedded filesystem and map-reduce cluster");
		// Make sure the log folder exists, otherwise the test fill fail
	    new File(TEST_LOG_DIR).mkdirs();
	    
	    System.setProperty("hadoop.log.dir", TEST_LOG_DIR);
	    System.setProperty("javax.xml.parsers.SAXParserFactory",
	    		"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
	    
	    Configuration conf = new Configuration();
	    conf.set("dfs.datanode.data.dir.perm", "775");
	    dfsCluster = new MiniDFSCluster(conf, 1, true, null);
	    dfsCluster.getFileSystem().makeQualified(input);
	    dfsCluster.getFileSystem().makeQualified(output);
	    
	    mrCluster = new MiniMRCluster(1, dfsCluster.getFileSystem().getUri().toString(), 1);
	    transferFilesToSequenceFile();
	}	
	
	private void transferFilesToSequenceFile() throws IOException, IllegalAccessException, InstantiationException {
		log.info("Copying files to storage cluster");
		
		FileSystem hdfs = dfsCluster.getFileSystem();
		
		// Write
		SequenceFile.Writer writer = SequenceFile.createWriter(
				hdfs, 
				hdfs.getConf(),
				input,
				Text.class,
				BytesWritable.class,
				SequenceFile.CompressionType.BLOCK);
			
		for (File f : new File(TEST_INPUT_PATH).listFiles()) {
			log.info("Writing " + f.getAbsolutePath());
			
			// Key = filename
			Text key = new Text(f.getName());
			
			// Value = binary data
	        BytesWritable value = new BytesWritable(IOUtils.toByteArray(new FileReader(f)));
	        writer.append(key, value);
		}
		writer.close();
		
		// Verify
		SequenceFile.Reader reader = new SequenceFile.Reader(hdfs, input, hdfs.getConf());
		Text key = (Text) reader.getKeyClass().newInstance();
		BytesWritable value = (BytesWritable) reader.getValueClass().newInstance();
		
		List<String> storedFiles = new ArrayList<String>();
		while (reader.next(key, value)) {
			storedFiles.add(key.toString());
		}
		reader.close();
		
		Assert.assertEquals(3, storedFiles.size());
		Assert.assertTrue(storedFiles.contains("creative-commons.png"));
		Assert.assertTrue(storedFiles.contains("creative-commons.pdf"));
		Assert.assertTrue(storedFiles.contains("creative-commons.jpg"));
		log.info("All testfiles transferred to storage cluster");
	}
	
	private JobConf createJobConf() {
		JobConf jobConf = mrCluster.createJobConf();
		jobConf.setJobName("unitTest");
		
		jobConf.setJarByClass(BackupJobRunner.class);
		jobConf.setMapRunnerClass(BackupJobRunner.class);
		jobConf.setInputFormat(SequenceFileInputFormat.class);
		SequenceFileInputFormat.setInputPaths(jobConf, input);
		FileOutputFormat.setOutputPath(jobConf, output);
		
		jobConf.setSpeculativeExecution(false);
		return jobConf;
	}
	
	@Test
	public void testJobExecution() throws Exception {
		log.info("Scheduling job NOW!");
	    JobClient.runJob(createJobConf());
	}
	
	@After
	public void tearDown() throws Exception {
		if (dfsCluster != null) {
			dfsCluster.shutdown();
			dfsCluster = null;
	    }
		
	    if (mrCluster != null) {
	    	mrCluster.shutdown();
	    	mrCluster = null;
	    }
	    
	    FileUtils.deleteDirectory(new File(TEST_LOG_DIR));
	    FileUtils.deleteDirectory(new File(TEST_OUTPUT_PATH));
	}
	
}
