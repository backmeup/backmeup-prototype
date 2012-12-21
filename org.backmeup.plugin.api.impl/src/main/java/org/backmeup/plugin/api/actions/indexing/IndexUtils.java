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
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

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
	
	public static final String FIELD_JOB_NAME = "job_name";
	
	public static final String FIELD_FULLTEXT = "fulltext";
	
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
	  fi.setFileId(owner + ":" + hash + ":" + timestamp);
	  fi.setSource(source.get(FIELD_BACKUP_SOURCES).toString());
	  fi.setTimeStamp(timestamp.longValue());
	  fi.setTitle(source.get(FIELD_FILENAME).toString());
	  fi.setPath(source.get(FIELD_PATH).toString());
	  fi.setSink(source.get(FIELD_BACKUP_SINK).toString());
	  Object contentType = source.get(FIELD_CONTENT_TYPE);
	  if (contentType != null)
		  fi.setType(getTypeFromMimeType(contentType.toString()));
	  else
		  fi.setType(getTypeFromMimeType("other"));
	  return fi;
	}
	
	public static List<SearchEntry> convertSearchEntries(org.elasticsearch.action.search.SearchResponse esResponse) {	    
	    List<SearchEntry> entries = new ArrayList<SearchResponse.SearchEntry>();
	    for (SearchHit hit : esResponse.getHits()) {
	    	Map<String, Object> source = hit.getSource();
	    	
			StringBuilder preview = null;
			HighlightField highlight = hit.getHighlightFields().get(IndexUtils.FIELD_FULLTEXT);
			if (highlight != null) {
				preview = new StringBuilder("... ");
				for (Text fragment : highlight.fragments()) {
					preview.append(fragment.string().replace("\n", " ").trim() + " ... ");
				}
			}	

	    	String hash = source.get(FIELD_FILE_HASH).toString();
	    	Integer owner = (Integer) source.get(FIELD_OWNER_ID);
	    	Long timestamp = (Long) source.get(FIELD_BACKUP_AT);
	    	
	    	SearchEntry entry = new SearchEntry();
	    	
	    	// We're constructing a (reasonably) unique ID using owner, hash and timestamp
	    	entry.setFileId(owner + ":" + hash + ":" + timestamp);
	    	entry.setTitle(source.get(FIELD_FILENAME).toString());
	    	entry.setTimeStamp(new Date(timestamp));
	    	
	    	if (source.get(FIELD_BACKUP_SOURCES) != null)
	    		entry.setDatasource(source.get(FIELD_BACKUP_SOURCES).toString());
	    	
	    	if (source.get(FIELD_JOB_NAME) != null)
	    		entry.setJobName(source.get(FIELD_JOB_NAME).toString());
	    	
			if (preview != null)
				entry.setPreviewSnippet(preview.toString().trim());
	    	
	    	Object contentType = source.get(FIELD_CONTENT_TYPE);
	    	if (contentType != null) {
	    		entry.setType(getTypeFromMimeType(contentType.toString()));
	    	} else {
	    		entry.setType("other");
	    	}
	    	
	    	entry.setProperty(FIELD_PATH, source.get(FIELD_PATH).toString());
	    	
	    	if (source.get(FIELD_BACKUP_SINK) != null)
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
		// return groupByField(esResponse, FIELD_CONTENT_TYPE);
		return groupByContentType(esResponse);
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
	
	private static List<CountedEntry> groupByContentType(org.elasticsearch.action.search.SearchResponse esResponse) {
		// Now where's my Scala groupBy!? *heul*
		Map<String, Integer> groupedHits = new HashMap<String, Integer>();
		for (SearchHit hit : esResponse.getHits()) {
			String type;
			if (hit.getSource().get(FIELD_CONTENT_TYPE) != null) {
				type = getTypeFromMimeType(hit.getSource().get(FIELD_CONTENT_TYPE).toString());
			} else {
				type = "other";
			}
			
			Integer count = groupedHits.get(type);
			if (count == null) {
				count = Integer.valueOf(1);
			} else {
				count = Integer.valueOf(count.intValue() + 1);
			}
			groupedHits.put(type, count);
		}
		
		// ...and .map
		List<CountedEntry> countedEntries = new ArrayList<SearchResponse.CountedEntry>();
		for (Entry<String, Integer> entry : groupedHits.entrySet()) {
			countedEntries.add(new CountedEntry(entry.getKey(), entry.getValue().intValue()));
		}
		
		return countedEntries;			
	}
	
	private static String getTypeFromMimeType(String mime) {
		mime = mime.toLowerCase();
		
		if (mime.contains("html"))
			return "html";
		
		if (mime.startsWith("image"))
			return "image";
					
		if (mime.startsWith("video"))
			return "video";
		
		if (mime.startsWith("audio"))
			return "audio";
		
		if (mime.startsWith("text"))
			return "text";
		
		if (mime.contains("pdf"))
			return "text";
		
		if (mime.contains("ogg"))
			return "audio";
					
		// Add more special rules as needed 
		
		return "other";
					
	}

}
