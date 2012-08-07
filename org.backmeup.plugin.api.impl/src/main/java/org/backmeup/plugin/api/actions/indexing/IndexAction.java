package org.backmeup.plugin.api.actions.indexing;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageReader;
import org.elasticsearch.client.Client;

public class IndexAction implements Action {
	
	private Client client;
	
	private String index;
	
	public IndexAction(Client client, String index) {
		this.client = client;
		this.index = index;
	}

	private static final String START_INDEX_PROCESS = "Starting index process";
	private static final String ANALYZING = "Analyzing data object ";
	private static final String INDEXING = "Indexing data object ";
	private static final String INDEX_PROCESS_COMPLETE = "Indexing complete";
	
	@Override
	public String doAction(Properties accessData, StorageReader storage, Progressable progressor)
			throws ActionException {
		
		progressor.progress(START_INDEX_PROCESS);

		TikaAnalyzer analyzer = new TikaAnalyzer();
		
		try {
			Iterator<DataObject> dataObjects = storage.getDataObjects();
			while (dataObjects.hasNext()) {
				DataObject dob = dataObjects.next();
				progressor.progress(ANALYZING + dob.getPath());
				
				// TODO Should the analyzer pack metainfo directly into the MetainfoContainer?
				Map<String, String> meta = analyzer.analyze(dob);

				progressor.progress(INDEXING + dob.getPath());				
				ElasticSearchIndexer indexer = new ElasticSearchIndexer(client, index);
				indexer.doIndexing(dob, meta);
			}
		} catch (Exception e) {
			throw new ActionException(e);
		}
		
		progressor.progress(INDEX_PROCESS_COMPLETE);
		
		return "";
	}

}
