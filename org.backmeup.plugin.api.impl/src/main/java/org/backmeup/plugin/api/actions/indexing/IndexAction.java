package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class IndexAction implements Action {
	private final Logger logger = LoggerFactory.getLogger(IndexAction.class);
	
	private Client client;
	
	public IndexAction(Client client) {
		this.client = client;
	}

	private static final String START_INDEX_PROCESS = "Starting index process";
	private static final String ANALYZING = "Analyzing data object ";
	private static final String SKIPPING = "This filetype is not indexed";
	private static final String INDEXING = "Indexing data object ";
	private static final String INDEX_PROCESS_COMPLETE = "Indexing complete";
	
	@Override
	public void doAction(Properties parameters, Storage storage, BackupJob job, Progressable progressor)
			throws ActionException {
		
		logger.debug("Starting file analysis...");
		progressor.progress(START_INDEX_PROCESS);

		TikaAnalyzer analyzer = new TikaAnalyzer();
		
		Date indexingTimestamp = new Date(); 
		
		try {
			Iterator<DataObject> dataObjects = storage.getDataObjects();
			while (dataObjects.hasNext()) {
				DataObject dob = dataObjects.next();
				progressor.progress(ANALYZING + dob.getPath());
				
				if (needsIndexing(dob)) {				
					Map<String, String> meta = analyzer.analyze(dob);
					String mime = meta.get("Content-Type");
					String fulltext = null;
					if (mime != null) {
						fulltext = extractFullText(dob, meta.get("Content-Type"));
					}
	
					progressor.progress(INDEXING + dob.getPath());				
					ElasticSearchIndexer indexer = new ElasticSearchIndexer(client);
					
					// TODO username needs to be available to action
					logger.debug("Indexing " + dob.getPath());
					meta = new HashMap<String, String>();
					meta.put(IndexUtils.FIELD_CONTENT_TYPE, mime);
					if (fulltext != null)
						meta.put(IndexUtils.FIELD_FULLTEXT, fulltext);
					
					indexer.doIndexing(job, dob, meta, indexingTimestamp);
				} else {
					progressor.progress(SKIPPING);
				}
			}
		} catch (Exception e) {
			throw new ActionException(e);
		}
		
		progressor.progress(INDEX_PROCESS_COMPLETE);			
	}
	
	private boolean needsIndexing(DataObject dob) {
		// TODO make this list configurable
		if (dob.getPath().endsWith(".css"))
			return false;
		
		if (dob.getPath().endsWith(".xsd"))
			return false;
		
		return true;
	}
	
	private String extractFullText(DataObject dob, String contentType) throws IOException, SAXException, TikaException {
        ContentHandler handler = new BodyContentHandler(10*1024*1024);
        Metadata metadata = new Metadata();
        
        AutoDetectParser parser = new AutoDetectParser();
        parser.parse(new ByteArrayInputStream(dob.getBytes()), handler, metadata, new ParseContext());
        return handler.toString();
	}
	
	/*
	private String extractFullText_HTML(DataObject dob) throws IOException, SAXException, TikaException {		
        ContentHandler handler = new BodyContentHandler(10*1024*1024);
        Metadata metadata = new Metadata();
        new HtmlParser().parse(new ByteArrayInputStream(dob.getBytes()), handler, metadata, new ParseContext());
        return handler.toString();
	}
	*/

}
