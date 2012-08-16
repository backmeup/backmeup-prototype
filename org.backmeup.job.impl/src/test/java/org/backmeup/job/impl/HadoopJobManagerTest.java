package org.backmeup.job.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
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
	    transferFiles();
	}	
	
	private void transferFiles() throws IOException {
		System.out.println("Copying files to storage cluster");
		
		// Wouldn't a Scala .map one-liner be nice here...
		// http://topsy.com/twitter.com/natpryce/status/223443583557042176
		List<Path> srcFiles = new ArrayList<Path>(); 
		for (File f : new File(TEST_INPUT_PATH).listFiles()) {
			srcFiles.add(new Path(f.getAbsolutePath()));
		}
		
		FileSystem fs = dfsCluster.getFileSystem();
		fs.copyFromLocalFile(false, true, srcFiles.toArray(new Path[srcFiles.size()]), input);
		System.out.println("Done.");
	}
	
	private JobConf createJobConf() {
		JobConf jobConf = mrCluster.createJobConf();
		jobConf.setJobName("unitTest");
		
		jobConf.setJarByClass(BackupJobRunner.class);
		jobConf.setMapRunnerClass(BackupJobRunner.class);	
		jobConf.setInputFormat(TextInputFormat.class);
		TextInputFormat.setInputPaths(jobConf, input);
		
		// We just set the output path to make hadoop happy.
		TextOutputFormat.setOutputPath(jobConf, output);
		
		jobConf.setSpeculativeExecution(false);
		return jobConf;
	}
	
	@Test
	public void testJobExecution() throws Exception {
		System.out.println("Scheduling job now.");
		
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
