package org.backmeup.plugin.api.actions.indexing;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageReader;

public class IndexAction implements Action {

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
				
				// TODO I assume the analyzer should pack metainfo directly into
				// the dobs MetainfoContainer?
				Map<String, String> meta = analyzer.analyze(dob);
				for (String key : meta.keySet()) {
					// Do something with the Tika metadata...
				}
				
				progressor.progress(INDEXING + dob.getPath());
				
				// TODO feed into ElasticSearch
			}
		} catch (Exception e) {
			throw new ActionException(e);
		}
		
		progressor.progress(INDEX_PROCESS_COMPLETE);
		
		return "";
	}

}
