package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.elasticsearch.client.Client;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

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
		IndexResponse response = client.prepareIndex(index, DOCUMENT_TYPE_BACKUP)
				.setSource(contentBuilder).execute().actionGet();	
	}

}
