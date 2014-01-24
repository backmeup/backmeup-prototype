package org.backmeup.logic.index;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchNodeFactory {
	private final Logger logger = LoggerFactory.getLogger(ElasticsearchNodeFactory.class);
	
	@Inject
	@Configuration(key = "backmeup.index.host", defaultValue = "localhost")
	private String indexHost;

	@Inject
	@Configuration(key = "backmeup.index.port", defaultValue = "9300")
	private Integer indexPort;
	
	@Inject
	@Configuration(key = "backmeup.index.name")
	private String indexName;

	@Inject
	@Configuration(key = "backmeup.index.node.data", defaultValue = "true")
	private Boolean nodeHoldData;

	@Inject
	@Configuration(key = "backmeup.index.node.data.path", mandatory = true)
	private String indexDataPath;

	@Inject
	@Configuration(key = "backmeup.index.cluster.name")
	private String clusterName;

	@Inject
	@Configuration(key = "backmeup.index.teststorage")
	private Boolean useTestStorage;
	
	private Node node;

	@Produces
	public Node getElasticSearchNode() {
		if (node == null) {
			node = initializeNode();
		}

		return node;
	}
	
	// on shutdown stop the node
	public void closeNode(@Disposes Node node) {
		logger.debug("Closing elasticsearch node");
		node.close();
	}
	
	private Node initializeNode(){
		Builder settingsBuilder = ImmutableSettings.settingsBuilder().put(
				"node.http.enabled", true);

		settingsBuilder.put("path.data", indexDataPath);
		settingsBuilder.put("cluster.name", clusterName);

		if (useTestStorage) {
			settingsBuilder.put("gateway.type", "none")
					.put("index.store.type", "memory")
					.put("index.number_of_shards", 1)
					.put("index.number_of_replicas", 0);
		}

		// .put("path.logs","target/elasticsearch/logs")
		Settings settings = settingsBuilder.build();

		// Decide whether node should hold data or not.
		if (nodeHoldData) {
			// node allocates indices and shards
			// 'local' Node: local discovery and transport, local to JVM
			// for unit/integration tests
			return NodeBuilder.nodeBuilder().settings(settings).node();
		} else {
			// node just as client
			return NodeBuilder.nodeBuilder().client(true)
					.settings(settings).node();
		}
	}
	
	/*
	@Produces
	public Client getElasticSearchClient() {
		if(node == null){
			node = initializeNode();
		}
		return node.client();
	}
	
	// close elasticsearch client
	public void closeClient(@Disposes Client client) {
		logger.debug("Closingx elasticsearch client");
		client.close();
	}
	
	private TransportClient createTransportClient() {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", clusterName).build();
		TransportClient client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(indexHost,indexPort));
		return client;
	}
	*/
}
