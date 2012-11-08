package org.backmeup.plugin.api.actions.indexing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.backmeup.model.FileItem;
import org.backmeup.model.ProtocolDetails.FileInfo;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;
import org.elasticsearch.search.SearchHit;

public class IndexUtils {
	
	public static final String FIELD_OWNER_ID = "owner_id";
	
	public static final String FIELD_OWNER_NAME = "owner_name";
	
	public static final String FIELD_FILENAME = "filename";
	
	public static final String FIELD_PATH = "path";
	
	public static final String FIELD_THUMBNAIL_PATH = "thumbnail_path";
	
	public static final String FIELD_BACKUP_SOURCES = "backup_sources";
	
	public static final String FIELD_BACKUP_SINK = "backup_sink";
	
	public static final String FIELD_FILE_HASH = "file_md5_hash";
	
	public static final String FIELD_BACKUP_AT = "backup_at";
	
	public static final String FIELD_CONTENT_TYPE = "Content-Type";
	
	public static final String FIELD_JOB_ID = "job_id";
	
	public static Set<FileItem> convertToFileItems(org.elasticsearch.action.search.SearchResponse esResponse) {
		Set<FileItem> fItems = new HashSet<FileItem>();
		
		for (SearchHit hit : esResponse.getHits()) {
			FileItem fileItem = new FileItem();
			Map<String, Object> source = hit.getSource();
			
	    	String hash = source.get(FIELD_FILE_HASH).toString();
	    	Integer owner = (Integer) source.get(FIELD_OWNER_ID);
	    	Long timestamp = (Long) source.get(FIELD_BACKUP_AT);
			
	    	String fileId = owner + ":" + hash + ":" + timestamp;
			fileItem.setFileId(fileId);
			fileItem.setTitle(source.get(FIELD_FILENAME).toString());
			fileItem.setTimeStamp(new Date(timestamp));
			
			if (source.get(FIELD_THUMBNAIL_PATH) != null)
				fileItem.setThumbnailURL("thumbnails/" + owner + "/" + fileId);
			
			fItems.add(fileItem);
		}
		
		return fItems;
	}
	
	public static FileInfo convertToFileInfo(org.elasticsearch.action.search.SearchResponse esResponse) {
	  if (esResponse.getHits().totalHits() == 0)
	    return null;
	  
	  SearchHit hit = esResponse.getHits().getHits()[0];
	  Map<String, Object> source = hit.getSource();
	  String hash = source.get(FIELD_FILE_HASH).toString();
    Integer owner = (Integer) source.get(FIELD_OWNER_ID);
    Long timestamp = (Long) source.get(FIELD_BACKUP_AT);
	  FileInfo fi = new FileInfo();
	  fi.setFileId(owner + hash + timestamp);
	  fi.setSource(source.get(FIELD_BACKUP_SOURCES).toString());
	  fi.setTimeStamp(source.get(FIELD_BACKUP_AT).toString());
	  fi.setTitle(source.get(FIELD_FILENAME).toString());
	  fi.setPath(source.get(FIELD_PATH).toString());
	  fi.setSink(source.get(FIELD_BACKUP_SINK).toString());
	  fi.setType(source.get(FIELD_CONTENT_TYPE).toString());	  
	  return fi;
	}
	
	public static List<SearchEntry> convertSearchEntries(org.elasticsearch.action.search.SearchResponse esResponse) {	    
	    List<SearchEntry> entries = new ArrayList<SearchResponse.SearchEntry>();
	    for (SearchHit hit : esResponse.getHits()) {
	    	Map<String, Object> source = hit.getSource();

	    	String hash = source.get(FIELD_FILE_HASH).toString();
	    	Integer owner = (Integer) source.get(FIELD_OWNER_ID);
	    	Long timestamp = (Long) source.get(FIELD_BACKUP_AT);
	    	
	    	SearchEntry entry = new SearchEntry();
	    	
	    	// We're constructing a (reasonably) unique ID using owner, hash and timestamp
	    	entry.setFileId(owner + ":" + hash + ":" + timestamp);
	    	entry.setTitle(source.get(FIELD_FILENAME).toString());
	    	entry.setTimeStamp(new Date(timestamp));
	    	
	    	Object contentType = source.get(FIELD_CONTENT_TYPE);
	    	if (contentType != null) {
	    		entry.setType(contentType.toString());
	    	} else {
	    		entry.setType("[unknown]");
	    	}
	    	
	    	entry.setProperty(FIELD_PATH, source.get(FIELD_PATH).toString());
	    	entry.setProperty(FIELD_BACKUP_SOURCES, source.get(FIELD_BACKUP_SOURCES).toString());
	    	entry.setProperty(FIELD_BACKUP_SINK, source.get(FIELD_BACKUP_SINK).toString());
	    	entry.setProperty(FIELD_FILE_HASH, hash);
	    	
	    	entries.add(entry);
	    }
		return entries;
	}
	
	public static List<CountedEntry> getBySource(org.elasticsearch.action.search.SearchResponse esResponse) {		
		// TODO we currently group by 'list of sources' rather than source
		return groupByField(esResponse, FIELD_BACKUP_SOURCES);
	}
	
	public static List<CountedEntry> getByType(org.elasticsearch.action.search.SearchResponse esResponse) {
		return groupByField(esResponse, FIELD_CONTENT_TYPE);
	}
	
	private static List<CountedEntry> groupByField(org.elasticsearch.action.search.SearchResponse esResponse, String field) {
		// Now where's my Scala groupBy!? *heul*
		Map<String, Integer> groupedHits = new HashMap<String, Integer>();
		for (SearchHit hit : esResponse.getHits()) {
			if (hit.getSource().get(field) != null) {
				String sourceName = hit.getSource().get(field).toString();
				Integer count = groupedHits.get(sourceName);
				if (count == null) {
					count = Integer.valueOf(1);
				} else {
					count = Integer.valueOf(count.intValue() + 1);
				}
				groupedHits.put(sourceName, count);
			}
		}
		
		// ...and .map
		List<CountedEntry> countedEntries = new ArrayList<SearchResponse.CountedEntry>();
		for (Entry<String, Integer> entry : groupedHits.entrySet()) {
			countedEntries.add(new CountedEntry(entry.getKey(), entry.getValue().intValue()));
		}
		
		return countedEntries;		
	}

}
