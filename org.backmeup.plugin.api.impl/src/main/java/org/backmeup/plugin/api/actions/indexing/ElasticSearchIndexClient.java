package org.backmeup.plugin.api.actions.indexing;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchIndexClient {
	
	private static final String INDEX_NAME = "backmeup";
	
	private Client client;
	
	public ElasticSearchIndexClient(String host, int port) {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "es-cluster-" + NetworkUtils.getLocalAddress().getHostName()).build();
		client = new TransportClient(settings)
			.addTransportAddress(new InetSocketTransportAddress(host, port));
	}
	
	public ElasticSearchIndexClient(Client client) {
		this.client = client;
	}
	
	public SearchResponse queryBackup(Long userId, String query) {
		String queryString = null;
		String[] tokens = query.split(" ");
		if (tokens.length == 0) {
			queryString = "*";
		} else if (tokens.length == 1) {
			queryString = query;
		} else {
			if (query.contains("AND") || query.contains("OR")) {
				queryString = query;
			} else {
				StringBuffer sb = new StringBuffer("*");
				for (int i=0; i<tokens.length; i++) {
					sb.append(tokens[i]);
					if (i < tokens.length - 1)
						sb.append("* AND *");
				}
				queryString = sb.toString() + "*";
			}
		}
	  
		QueryBuilder qBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery(IndexUtils.FIELD_OWNER_ID, userId))
				.must(QueryBuilders.queryString(queryString));
		
		return client.prepareSearch(INDEX_NAME).setQuery(qBuilder).setSize(10000).execute().actionGet();
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
	
	public void deleteRecordsForUser(Long userId) {
		client.prepareDeleteByQuery(INDEX_NAME)
				.setQuery(QueryBuilders.matchQuery(IndexUtils.FIELD_OWNER_ID, userId))
				.execute().actionGet();
	
	}
	
	public void close() {
		if (client != null)
			client.close();
	}
}
