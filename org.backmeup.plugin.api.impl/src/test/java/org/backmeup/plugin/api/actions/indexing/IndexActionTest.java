package org.backmeup.plugin.api.actions.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DummyStorageReader;
import org.backmeup.plugin.api.storage.StorageReader;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IndexActionTest {
	
	private static Node node;
	
	private static final String ELASTICSEARCH_CLUSTERNAME = "testcluster";
	
	private static final String BACKUP_JOB =
			"{\"user\":{\"userId\":1,\"username\":\"Sepp\",\"password\":\"pw\"," + 
	        "\"keyRing\":\"k3yr1nG\",\"email\":\"e@ma.il\",\"isActivated\":false,\"properties\":[]}," +
			"\"sourceProfiles\":" +
			"[{\"profile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"Sepp\"," +
			"\"password\":\"pw\",\"keyRing\":\"k3yr1nG\",\"email\":\"e@ma.il\",\"isActivated\":" +
			"false,\"properties\":[]},\"profileName\":\"TestProfile\",\"desc\":" +
			"\"org.backmeup.dummy\",\"sourceAndOrSink\":\"Source\"},\"options\":" + 
			"[\"folder1\",\"folder2\"]}]," +
			"\"sinkProfile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"Sepp\"" +
			",\"password\":\"pw\",\"keyRing\":\"pw\",\"email\":\"e@ma.il\",\"isActivated\":" +
			"false,\"properties\":[]},\"profileName\":\"TestProfile2\",\"desc\":" +
			"\"org.backmeup.dummy\",\"sourceAndOrSink\":\"Sink\"},\"requiredActions\":[]," + 
			"\"start\":\"1345203377704\",\"delay\":1345203258212}";
	
	private Progressable logProgressable = new Progressable() {
		@Override
		public void progress(String message) {
			System.out.println("PROGRESS: " + message);
		}
	};
	
	@Before
	public void setup() throws ActionException {
		node = NodeBuilder.nodeBuilder().local(true).clusterName(ELASTICSEARCH_CLUSTERNAME).node();		
		
		System.out.println("Setting up test index...");
		// Dummy storage reader on the src/test/resources directory
		StorageReader reader = new DummyStorageReader();
		
		// Local ElasticSearch node
		Client client = node.client();
	  
		// Index test files on the local ES index
		IndexAction action = new IndexAction(client);
		
		BackupJob job = JsonSerializer.deserialize(BACKUP_JOB, BackupJob.class);
		
		action.doAction(null, reader, null, job, logProgressable);
		System.out.println("Done.");
	}
	
	@After
	public void tearDown() throws IOException {
		node.close();
		FileUtils.deleteDirectory(new File("data"));
	}
	
	@Test
	public void verifyIndex() {
		System.out.println("Verifying indexing content");
		Client client = node.client();
		SearchResponse response = client.prepareSearch("backmeup").setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
		SearchHits hits = response.getHits();
		Assert.assertEquals(3, hits.getHits().length);
		
		for (SearchHit hit : response.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			for (String key : source.keySet()) {
				System.out.println(key + ": " + source.get(key));
				
				if (key.equals("owner_name"))
					Assert.assertEquals("Sepp", source.get(key));
				
				if (key.equals("owner_id"))
					Assert.assertEquals(1, source.get(key));
				
				if (key.equals("backup_sources"))
					Assert.assertEquals("TestProfile", source.get(key));
				
				if (key.equals("backup_sink"))
					Assert.assertEquals("TestProfile2", source.get(key));
				
				if (key.equals("path"))
					Assert.assertTrue(source.get(key).toString().startsWith("src"));
			}
		}
		
		System.out.println("Done.");
	}
	
	@Test
	public void verifyQuery() {
		System.out.println("Verifying keyword search");
		Client client = node.client();
		
		ElasticSearchIndexClient idx = new ElasticSearchIndexClient(client);
		SearchResponse response = idx.queryBackup("Sepp", "creative-commons");
		
		for (SearchHit hit : response.getHits()) {
			System.out.println(hit.getSourceAsString());
		}
		
		Assert.assertEquals(3, response.getHits().totalHits());
	}

}
