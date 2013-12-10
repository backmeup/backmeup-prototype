package org.backmeup.plugin.api.actions.indexing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FileItem;
import org.backmeup.model.ProtocolDetails.FileInfo;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexUtils {
	private static final Logger logger = LoggerFactory.getLogger(IndexUtils.class);
	
	public static final String FIELD_OWNER_ID = "owner_id";
	
	public static final String FIELD_OWNER_NAME = "owner_name";
	
	public static final String FIELD_FILENAME = "filename";
	
	public static final String FIELD_PATH = "path";
	
	public static final String FIELD_THUMBNAIL_PATH = "thumbnail_path";
	
	public static final String FIELD_BACKUP_SOURCE_ID = "backup_source_id";
	
	public static final String FIELD_BACKUP_SOURCE_IDENTIFICATION = "backup_source_identification";
	
	public static final String FIELD_BACKUP_SOURCE_PLUGIN_NAME = "backup_source_plugin_name";
	
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
	  fi.setSource(source.get(FIELD_BACKUP_SOURCE_PLUGIN_NAME) + " (" + source.get(FIELD_BACKUP_SOURCE_IDENTIFICATION) + ")");
	  fi.setSourceId(Long.valueOf((Integer) source.get(FIELD_BACKUP_SOURCE_ID)));
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
	
	public static List<SearchEntry> convertSearchEntries(org.elasticsearch.action.search.SearchResponse esResponse, BackMeUpUser user) {	    
	    List<SearchEntry> entries = new ArrayList<SearchResponse.SearchEntry>();

	    logger.debug("converting " + esResponse.getHits().totalHits() + " search results");
	    
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
	    	
	    	if (source.get(FIELD_BACKUP_SOURCE_ID) != null) {
	    		entry.setDatasourceId(Long.valueOf(((Integer) source.get(FIELD_BACKUP_SOURCE_ID))));
	    		entry.setDatasource(source.get(FIELD_BACKUP_SOURCE_PLUGIN_NAME) + " (" + source.get(FIELD_BACKUP_SOURCE_IDENTIFICATION) + ")");
	    	}
	    	
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
	    	
			if (source.get(FIELD_THUMBNAIL_PATH) != null)
				entry.setThumbnailUrl("thumbnails/" + user.getUsername() + "/" + owner + ":" + hash + ":" + timestamp);
			
			// Custom props
			if (source.get("destination") != null)
				entry.setProperty("destination", source.get("destination").toString());
			
			if (source.get("message") != null)
				entry.setProperty("message", source.get("message").toString());
			
			if (source.get("parent") != null)
				entry.setProperty("parent", source.get("parent").toString());
			
			if (source.get("author") != null)
				entry.setProperty("author", source.get("author").toString());
			
			if (source.get("source") != null)
				entry.setProperty("source", source.get("source").toString());
			
			if (source.get("likes") != null)
				entry.setProperty("likes", source.get("likes").toString());
			
			if (source.get("tags") != null)
				entry.setProperty("tags", source.get("tags").toString());
			
			if (source.get("modified") != null)
				entry.setProperty("modified", source.get("modified").toString());
			
	    	entries.add(entry);
	    }
		return entries;
	}
	
	public static List<CountedEntry> getBySource(org.elasticsearch.action.search.SearchResponse esResponse) {		
		// TODO we currently group by 'list of sources' rather than source
		return groupBySource(esResponse);
	}
	
	public static List<CountedEntry> getByType(org.elasticsearch.action.search.SearchResponse esResponse) {
		return groupByContentType(esResponse);
	}
	
	public static List<CountedEntry> getByJob(org.elasticsearch.action.search.SearchResponse esResponse) {
		return groupByContentJob (esResponse);
	}
	
	private static List<CountedEntry> groupBySource(org.elasticsearch.action.search.SearchResponse esResponse) {
		// Now where's my Scala groupBy!? *heul*
		Map<String, Integer> groupedHits = new HashMap<String, Integer>();
		for (SearchHit hit : esResponse.getHits()) {
			if (hit.getSource().get(FIELD_JOB_ID) != null) {
				String backupSourcePluginName = hit.getSource().get(FIELD_BACKUP_SOURCE_PLUGIN_NAME).toString();
				String backupSourceIdentification = hit.getSource().get(FIELD_BACKUP_SOURCE_IDENTIFICATION).toString();
				String label = backupSourcePluginName + " (" + backupSourceIdentification + ")";
				Integer count = groupedHits.get(label);
				if (count == null) {
					count = Integer.valueOf(1);
				} else {
					count = Integer.valueOf(count.intValue() + 1);
				}
				groupedHits.put(label, count);
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
	
	private static List<CountedEntry> groupByContentJob (org.elasticsearch.action.search.SearchResponse esResponse)
	{
		// Now where's my Scala groupBy!? *heul*
		Map<String, Integer> groupedHits = new HashMap<String, Integer> ();
		for (SearchHit hit : esResponse.getHits ())
		{
			if (hit.getSource ().get (FIELD_JOB_ID) != null)
			{
				String backupJobName = hit.getSource ().get (FIELD_JOB_NAME).toString ();
				String backupTimestamp = hit.getSource ().get (FIELD_BACKUP_AT).toString ();
				String label = backupJobName + " (" + backupTimestamp + ")";
				Integer count = groupedHits.get (label);
				if (count == null)
				{
					count = Integer.valueOf (1);
				}
				else
				{
					count = Integer.valueOf (count.intValue () + 1);
				}
				groupedHits.put (label, count);
			}
		}

		// ...and .map
		List<CountedEntry> countedEntries = new ArrayList<SearchResponse.CountedEntry> ();
		for (Entry<String, Integer> entry : groupedHits.entrySet ())
		{
			countedEntries.add (new CountedEntry (entry.getKey (), entry.getValue ().intValue ()));
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

	public static String getFilterSuffix(Map<String, List<String>> filters) {
		if (filters == null)
			return "";
		
		String filterstr = "";
		
		if (filters.containsKey ("type") == true)
		{
			filterstr += "(";
			
			for(String filter : filters.get ("type"))
			{
				if (filter.toLowerCase().equals("html")) {
					filterstr += "Content-Type:*html* OR ";
				} else if (filter.toLowerCase().equals("image")) {
					filterstr += "Content-Type:image* OR ";
				} else if (filter.toLowerCase().equals("video")) {
					filterstr += "Content-Type:video* OR ";
				} else if (filter.toLowerCase().equals("audio")) {
					filterstr += "Content-Type:audio* OR ";
				} else if (filter.toLowerCase().equals("text")) {
					filterstr += "Content-Type:text* OR ";
				}
			}
			
			// remove the last " OR " and close the search string for this part
			filterstr = filterstr.substring (0, filterstr.length () - 4);
			filterstr += ") AND ";
		}
		
		// TODO if "ProfileName" includes special chars like (,", ... we will have a problem with the search?
		if (filters.containsKey ("source") == true)
		{
			filterstr += "(";
			
			// something like this will come "org.backmeup.source (ProfileName)"
			for (String filter : filters.get ("source"))
			{
				// get out the source plugin, result will be "org.backmeup.source"
				String source = filter.substring (0, filter.indexOf (" "));
				
				// get out the profile "(Profilename)"
				String profile = filter.substring (filter.indexOf (" ") + 1, filter.length ());
				// remove the brackets at begin and end, result will be "ProfileName"
				profile = profile.substring (1, profile.length () - 1);
				
				filterstr += "(" + FIELD_BACKUP_SOURCE_PLUGIN_NAME + ":" + source + " AND " + FIELD_BACKUP_SOURCE_IDENTIFICATION + ":" + profile + ") OR ";
			}
			
			// remove the last " OR " and close the search string for this part
			filterstr = filterstr.substring (0, filterstr.length () - 4);
			filterstr += ") AND ";
		}
		
		// TODO if job contains special chars ...
		if (filters.containsKey ("job") == true)
		{
			filterstr += "(";
			
			// something like this will come "JobName (Timestamp)" (java timestamp -> 13 chars)
			for (String filter : filters.get ("job"))
			{
				// get out the timestamp (also remove the "()").
				String timestamp = filter.substring (filter.length () - 14, filter.length () - 1);
				
				// get out the job name
				String jobname = filter.substring (0, filter.length () - 16);
				
				filterstr += "(" + FIELD_BACKUP_AT + ":" + timestamp + " AND " + FIELD_JOB_NAME + ":" + jobname + ") OR ";
			}
			
			// remove the last " OR " and close the search string for this part
			filterstr = filterstr.substring (0, filterstr.length () - 4);
			filterstr += ") AND ";
		}
		
		return filterstr;
	}
	
	
	
	
	public static QueryBuilder buildQuery (Long userid, String queryString, Map<String, List<String>> filters)
	{
		BoolQueryBuilder qBuilder = new BoolQueryBuilder();
		qBuilder.must (QueryBuilders.matchQuery (IndexUtils.FIELD_OWNER_ID, userid));
		qBuilder.must (QueryBuilders.queryString (queryString));
		
		if (filters == null)
		{
			return qBuilder;
		}
		
		BoolQueryBuilder typematches = buildTypeQuery (filters);
		BoolQueryBuilder sourcematches = buildSourceQuery (filters);
		BoolQueryBuilder jobmatches = buildJobQuery (filters);
		
		if (typematches != null)
		{
			qBuilder.must(typematches);
		}
		if (sourcematches != null)
		{
			qBuilder.must(sourcematches);
		}
		if (jobmatches != null)
		{
			qBuilder.must(jobmatches);
		}
		
		return qBuilder;
	}
	
	private static BoolQueryBuilder buildTypeQuery (Map<String, List<String>> filters)
	{
		BoolQueryBuilder typematches = null;

		if (filters.containsKey ("type") == true)
		{
			typematches = new BoolQueryBuilder();
			// minimum 1 of the should clausels must match
			typematches.minimumNumberShouldMatch (1);
			
			for (String filter : filters.get ("type"))
			{
				if (filter.toLowerCase ().equals ("html"))
				{
					typematches.should (QueryBuilders.matchPhraseQuery (FIELD_CONTENT_TYPE, "*html*"));
				}
				else if (filter.toLowerCase ().equals ("image"))
				{
					typematches.should (QueryBuilders.matchPhraseQuery (FIELD_CONTENT_TYPE, "image*"));
				}
				else if (filter.toLowerCase ().equals ("video"))
				{
					typematches.should (QueryBuilders.matchPhraseQuery (FIELD_CONTENT_TYPE, "video*"));
				}
				else if (filter.toLowerCase ().equals ("audio"))
				{
					typematches.should (QueryBuilders.matchPhraseQuery (FIELD_CONTENT_TYPE, "audio*"));
				}
				else if (filter.toLowerCase ().equals ("text"))
				{
					typematches.should (QueryBuilders.matchPhraseQuery (FIELD_CONTENT_TYPE, "text*"));
				}
			}
		}
		
		return typematches;
	}
	
	private static BoolQueryBuilder buildSourceQuery (Map<String, List<String>> filters)
	{
		BoolQueryBuilder sourcematches = null;
		
		if (filters.containsKey ("source") == true)
		{
			sourcematches = new BoolQueryBuilder();
			// minimum 1 of the should clausels must match
			sourcematches.minimumNumberShouldMatch (1);
			
			for (String filter : filters.get ("source"))
			{
				// get out the source plugin, result will be "org.backmeup.source"
				String source = filter.substring (0, filter.indexOf (" "));
				
				// get out the profile "(Profilename)"
				String profile = filter.substring (filter.indexOf (" ") + 1, filter.length ());
				
				// remove the brackets at begin and end, result will be "ProfileName"
				profile = profile.substring (1, profile.length () - 1);
				
				BoolQueryBuilder tempbuilder = new BoolQueryBuilder();
				tempbuilder.must (QueryBuilders.matchPhraseQuery (FIELD_BACKUP_SOURCE_PLUGIN_NAME, source));
				tempbuilder.must (QueryBuilders.matchPhraseQuery (FIELD_BACKUP_SOURCE_IDENTIFICATION, profile));
				
				// tempbuilder1 or tempbulder2 or ...
				sourcematches.should (tempbuilder);
			}
		}
		
		return sourcematches;
	}
	
	private static BoolQueryBuilder buildJobQuery (Map<String, List<String>> filters)
	{
		BoolQueryBuilder jobmatches = null;
		
		if (filters.containsKey ("job") == true)
		{
			jobmatches = new BoolQueryBuilder();
			// minimum 1 of the should clausels must match
			jobmatches.minimumNumberShouldMatch (1);
			
			// something like this will come "JobName (Timestamp)" (java timestamp -> 13 chars)
			for (String filter : filters.get ("job"))
			{
				// get out the timestamp (also remove the "()").
				String timestamp = filter.substring (filter.length () - 14, filter.length () - 1);
				
				// get out the job name
				String jobname = filter.substring (0, filter.length () - 16);
				
				BoolQueryBuilder tempbuilder = new BoolQueryBuilder();
				tempbuilder.must (QueryBuilders.matchPhraseQuery (FIELD_BACKUP_AT, timestamp));
				tempbuilder.must (QueryBuilders.matchPhraseQuery (FIELD_JOB_NAME, jobname));
				
				// tempbuilder1 or tempbulder2 or ...
				jobmatches.should (tempbuilder);
			}
		}
		
		return jobmatches;
	}
}
