package org.backmeup.job.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.apache.hadoop.mapred.FileOutputFormat;
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

		FileSystem hdfs = dfsCluster.getFileSystem();
		for (File f : new File(TEST_INPUT_PATH).listFiles()) {
			System.out.println("Copying " + f.getAbsolutePath());
			hdfs.copyFromLocalFile(new Path(f.getAbsolutePath()), new Path(input, f.getName()));
		}
				
		for (File f: new File(TEST_INPUT_PATH).listFiles()) {
			Path p = new Path(input, f.getName());
			System.out.print("Verifying copy: " + p.toString());
			Assert.assertTrue(hdfs.exists(p));
			System.out.println(" - OK");
		}
		System.out.println("Done.");
	}
	
	private JobConf createJobConf() {
		JobConf jobConf = mrCluster.createJobConf();
		jobConf.setJobName("unitTest");
		
		jobConf.setJarByClass(BackupJobRunner.class);
		jobConf.setMapRunnerClass(BackupJobRunner.class);
		FileInputFormat.setInputPaths(jobConf, input);
		FileOutputFormat.setOutputPath(jobConf, output);
		
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
