package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.StringText;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
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
	
	private static final String INDEX_NAME = "backmeup";
	
	private Client client;
	
	public ElasticSearchIndexer(String host, int port) {
		host = NetworkUtils.getLocalAddress().getHostName();
		String clusterName = "es-cluster-" + NetworkUtils.getLocalAddress().getHostName();

		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build();
		client = new TransportClient(settings)
			.addTransportAddress(new InetSocketTransportAddress(host, port));
	}

	public ElasticSearchIndexer(Client client) {
		this.client = client;
	}
	
	public void doIndexing(BackupJob job, DataObject dataObject, Map<String, String> meta) throws IOException {
		// Build the index object
		XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();
		
		for (String metaKey : meta.keySet()) {
			contentBuilder = contentBuilder.field(metaKey, meta.get(metaKey));
		}
		
		contentBuilder.field(IndexUtils.FIELD_OWNER_ID, job.getUser().getUserId());
		contentBuilder.field(IndexUtils.FIELD_OWNER_NAME, job.getUser().getUsername());
		contentBuilder.field(IndexUtils.FIELD_FILENAME, getFilename(dataObject.getPath()));
		contentBuilder.field(IndexUtils.FIELD_PATH, dataObject.getPath());
		contentBuilder.field(IndexUtils.FIELD_FILE_HASH, dataObject.getMD5Hash());
		contentBuilder.field(IndexUtils.FIELD_BACKUP_SINK, job.getSinkProfile().getIdentification());
		contentBuilder.field(IndexUtils.FIELD_BACKUP_AT, new Date().getTime());
		contentBuilder.field(IndexUtils.FIELD_JOB_ID, job.getId());
		
		// Where's my Scala .map and mkString!?!
		List<String> sourceNames = new ArrayList<String>(); 
		for (ProfileOptions source : job.getSourceProfiles()) {
			sourceNames.add(source.getProfile().getIdentification());			
		}
		contentBuilder.field(IndexUtils.FIELD_BACKUP_SOURCES, StringUtils.join(sourceNames, ", "));
		
		MetainfoContainer metainfoContainer = dataObject.getMetainfo();
		if (metainfoContainer != null) {
			Iterator<Metainfo> it = metainfoContainer.iterator();
			while (it.hasNext()) {
				Properties metainfo = it.next().getAttributes();
				for (Object key : metainfo.keySet()) {
					contentBuilder.field(key.toString(), new StringText(metainfo.get(key).toString()));
				}
			}
		}
		
		contentBuilder = contentBuilder.endObject();
		
		System.out.print("Pushing to ES index...");
		
		// Push to ES index
		client.prepareIndex(INDEX_NAME, DOCUMENT_TYPE_BACKUP).setSource(contentBuilder)
			.setRefresh(true).execute().actionGet();
		System.out.println(" done.");
	}
	
	private String getFilename(String path) {
		if (path.indexOf('/') > -1)
			return path.substring(path.lastIndexOf('/') + 1);
		
		return path;
	}

}
