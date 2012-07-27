package org.backmeup.plugin.api.actions.indexing;

import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DummyStorageReader;
import org.backmeup.plugin.api.storage.StorageReader;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;

public class IndexActionTest {
	
	private Progressable logProgressable = new Progressable() {
		@Override
		public void progress(String message) {
			System.out.println("PROGRESS: " + message);
		}
	};
	
	@Test
    public void testIndexAction() throws ActionException {
		// Dummy storage reader on the src/test/resources directory
		StorageReader reader = new DummyStorageReader();
		
		// Local ElasticSearch node
		Node node = NodeBuilder.nodeBuilder().local(true).node();
		Client client = node.client();
	  
		// Index test files on the local ES index
		IndexAction action = new IndexAction(client);
		action.doAction(null, reader, logProgressable);
		
		node.close();
	}

}
