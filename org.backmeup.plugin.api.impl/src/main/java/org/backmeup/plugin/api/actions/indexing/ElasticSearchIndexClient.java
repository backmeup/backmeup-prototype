package org.backmeup.plugin.api.actions.indexing;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

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
	
	public SearchResponse queryBackup(Long userId, String query) {
		QueryBuilder qBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery(IndexUtils.FIELD_OWNER_ID, userId))
				.must(QueryBuilders.queryString(query));
		
		return client.prepareSearch(INDEX_NAME).setQuery(qBuilder).execute().actionGet();
	}
	
	public SearchResponse searchByJobId(long jobId) {
		QueryBuilder qBuilder = QueryBuilders.matchQuery(IndexUtils.FIELD_JOB_ID, jobId);
		return client.prepareSearch(INDEX_NAME).setQuery(qBuilder).execute().actionGet();
	}
	
	public SearchResponse getFileById(String username, String fileId) {
		// IDs in backmeup are "owner:hash:timestamp"
		String[] bmuId = fileId.split(":");
		if (bmuId.length != 3)
			throw new IllegalArgumentException("Invalid file ID: " + fileId);
		
		Long owner = Long.parseLong(bmuId[0]);
		String hash = bmuId[1];
		Long timestamp = Long.parseLong(bmuId[2]);
		
		QueryBuilder qBuilder = QueryBuilders.boolQuery()
		    //.must(QueryBuilders.matchQuery(FIELD_OWNER_NAME, username))
				.must(QueryBuilders.matchQuery(IndexUtils.FIELD_OWNER_ID, owner))
				.must(QueryBuilders.matchQuery(IndexUtils.FIELD_FILE_HASH, hash))
				.must(QueryBuilders.matchQuery(IndexUtils.FIELD_BACKUP_AT, timestamp));
		
		return client.prepareSearch(INDEX_NAME).setQuery(qBuilder).execute().actionGet();
	}
	
	public String getThumbnailPathForFile(String username, String fileId) {
		SearchResponse response = getFileById(username, fileId);
		SearchHit hit = response.getHits().getHits()[0];
		Map<String, Object> source = hit.getSource();
		return source.get(IndexUtils.FIELD_THUMBNAIL_PATH).toString();
	}
}
