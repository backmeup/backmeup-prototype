package org.backmeup.plugin.api.actions.indexing;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ElasticSearchIndexClient {
	
	private static final String INDEX_NAME = "backmeup";
	
	private Client client;
	
	public ElasticSearchIndexClient(String host, int port) {
		client = new TransportClient()
			.addTransportAddress(new InetSocketTransportAddress(host, port));
	}
	
	public ElasticSearchIndexClient(Client client) {
		this.client = client;
	}
	
	public SearchResponse queryBackup(String username, String query) {
		QueryBuilder qBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("owner", username))
				.must(QueryBuilders.queryString(query));
		
		return client.prepareSearch(INDEX_NAME).setQuery(qBuilder).execute().actionGet();
	}

}
