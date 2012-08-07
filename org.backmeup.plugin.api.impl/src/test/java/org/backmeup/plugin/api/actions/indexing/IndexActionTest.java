package org.backmeup.plugin.api.actions.indexing;

import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DummyStorageReader;
import org.backmeup.plugin.api.storage.StorageReader;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IndexActionTest {
	
	private static Node node;
	
	private Progressable logProgressable = new Progressable() {
		@Override
		public void progress(String message) {
			System.out.println("PROGRESS: " + message);
		}
	};
	
	@BeforeClass
	public static void setup() {
		node = NodeBuilder.nodeBuilder().local(true).node();		
	}
	
	@AfterClass
	public static void tearDown() {
		node.close();
	}
	
	@Test
    public void testIndexAction() throws ActionException {
		// Dummy storage reader on the src/test/resources directory
		StorageReader reader = new DummyStorageReader();
		
		// Local ElasticSearch node
		Client client = node.client();
	  
		// Index test files on the local ES index
		IndexAction action = new IndexAction(client, "backmeup");
		action.doAction(null, reader, logProgressable);
	}
	
	@Test
	public void verifyIndex() {
		Client client = node.client();
		
		// TODO verify index content
		
	}

}
