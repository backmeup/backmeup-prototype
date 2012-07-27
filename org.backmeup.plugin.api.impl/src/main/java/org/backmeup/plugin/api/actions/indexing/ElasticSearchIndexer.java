package org.backmeup.plugin.api.actions.indexing;

import java.util.Map;

import org.backmeup.plugin.api.storage.DataObject;
import org.elasticsearch.client.Client;

public class ElasticSearchIndexer {
	
	private Client client;
	
	public ElasticSearchIndexer(Client client) {
		this.client = client;
	}
	
	public void doIndexing(DataObject dataObject, Map<String, String> meta) {
		// TODO implement
	}

}
