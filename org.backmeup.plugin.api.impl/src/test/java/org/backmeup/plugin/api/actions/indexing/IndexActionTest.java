package org.backmeup.plugin.api.actions.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
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
		action.doAction(null, reader, null, logProgressable);
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
				Assert.assertTrue(key.equals("Content-Type") || key.equals("owner") || key.equals("path"));
				// System.out.println(source.get(key));
				Assert.assertTrue(
						source.get(key).toString().startsWith("application/") ||
						source.get(key).toString().startsWith("image/") ||
						source.get(key).toString().startsWith("src") ||
						source.get(key).toString().equals("dummy"));
			}
		}
		
		System.out.println("Done.");
	}
	
	@Test
	public void verifyQuery() {
		System.out.println("Verifying keyword search");
		Client client = node.client();
		
		ElasticSearchIndexClient idx = new ElasticSearchIndexClient(client);
		SearchResponse response = idx.queryBackup("dummy", "creative-commons");
		
		for (SearchHit hit : response.getHits()) {
			System.out.println(hit.getSourceAsString());
		}
		
		Assert.assertEquals(3, response.getHits().totalHits());
	}

}
