package org.backmeup.plugin.api.actions.indexing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

public class IndexUtils {
	
	public static final String FIELD_OWNER_ID = "owner_id";
	
	public static final String FIELD_OWNER_NAME = "owner_name";
	
	public static final String FIELD_FILENAME = "filename";
	
	public static final String FIELD_PATH = "path";
	
	public static final String FIELD_BACKUP_SOURCES = "backup_sources";
	
	public static final String FIELD_BACKUP_SINK = "backup_sink";
	
	public static final String FIELD_FILE_HASH = "file_md5_hash";
	
	public static final String FIELD_BACKUP_AT = "backup_at";
	
	public static final String FIELD_CONTENT_TYPE = "Content-Type";
	
	public static List<SearchEntry> convertSearchEntries(org.elasticsearch.action.search.SearchResponse esResponse) {	    
	    List<SearchEntry> entries = new ArrayList<SearchResponse.SearchEntry>();
	    for (SearchHit hit : esResponse.getHits()) {
	    	SearchEntry entry = new SearchEntry();
	    	entry.setTitle(hit.field(FIELD_FILENAME).getValue().toString());
	    	entry.setTimeStamp((Date) hit.field(IndexUtils.FIELD_BACKUP_AT).getValue());
	    	
	    	SearchHitField contentType = hit.field(FIELD_CONTENT_TYPE);
	    	if (contentType != null) {
	    		entry.setType(contentType.getValue().toString());
	    	} else {
	    		entry.setType("[unknown]");
	    	}
	    	
	    	entry.setProperty(FIELD_PATH, hit.field(FIELD_PATH).toString());
	    	entry.setProperty(FIELD_BACKUP_SOURCES, hit.field(FIELD_BACKUP_SOURCES).toString());
	    	entry.setProperty(FIELD_BACKUP_SINK, hit.field(FIELD_BACKUP_SINK).toString());
	    	entry.setProperty(FIELD_FILE_HASH, hit.field(FIELD_FILE_HASH).toString());
	    }
		return entries;
	}
	
	public static List<CountedEntry> getBySource(org.elasticsearch.action.search.SearchResponse esResponse) {
		return null;
	}
	
	public static List<CountedEntry> getByType(org.elasticsearch.action.search.SearchResponse esResponse) {
		return null;
	}

}
