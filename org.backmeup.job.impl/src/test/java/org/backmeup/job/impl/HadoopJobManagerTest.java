package org.backmeup.job.impl;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HadoopJobManagerTest {

	private static final String TEST_LOG_DIR = "hadoop-logs";

	private MiniDFSCluster dfsCluster = null;
	private MiniMRCluster mrCluster = null;
	
	private final Path input = new Path("input");
	private final Path output = new Path("output");
	
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
	    System.out.println("Done.");
	}
	
	@Test
	public void testJobExecution() throws Exception {
		// TODO 
		System.out.println("testing...");
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
	}
	
}
