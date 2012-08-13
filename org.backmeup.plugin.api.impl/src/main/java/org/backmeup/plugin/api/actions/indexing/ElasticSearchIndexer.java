package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * The indexer must be handed an ElasticSearch client in order to work. (This must be done
 * by the class orchestrating the backup workflow!) Cf. IndexActionTest for an example on
 * how to create a client talking to an ad-hoc embedded ElasticSearch node.
 * 
 * To talk to an existing ElasticSearch cluster, I recommend using a TransportClient, like so:
 * 
 * Client client = new TransportClient()
 *      .addTransportAddress(new InetSocketTransportAddress("host1", 9300))
 *      .addTransportAddress(new InetSocketTransportAddress("host2", 9300));
 *       
 * It is possible to add arbitrary numbers of transport addresses - the client will
 * communicate with them in round-robin fashion.   
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class ElasticSearchIndexer {
	
	private static final String DOCUMENT_TYPE_BACKUP = "backup";
	
	private Client client;
	
	private String index;
	
	public ElasticSearchIndexer(Client client, String index) {
		this.client = client;
		this.index = index;
	}
	
	public void doIndexing(DataObject dataObject, Map<String, String> meta) throws IOException {
		// Build the index object
		XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();
		
		for (String metaKey : meta.keySet()) {
			contentBuilder = contentBuilder.field(metaKey, meta.get(metaKey));
		}
		
		MetainfoContainer metainfoContainer = dataObject.getMetainfo();
		if (metainfoContainer != null) {
			Iterator<Metainfo> it = metainfoContainer.iterator();
			while (it.hasNext()) {
				Properties metainfo = it.next().getAttributes();
				for (Object key : metainfo.keySet()) {
					contentBuilder.field(key.toString(), metainfo.get(key));
				}
			}
		}
		
		contentBuilder = contentBuilder.endObject();
		
		// Push to ES index
		client.prepareIndex(index, DOCUMENT_TYPE_BACKUP).setSource(contentBuilder)
			.setRefresh(true).execute().actionGet();	
	}

}
