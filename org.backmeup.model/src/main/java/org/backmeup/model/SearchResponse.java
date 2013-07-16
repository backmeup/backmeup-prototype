package org.backmeup.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Contains the result of a search when calling BusinessLogic#queryBackup
 * 
 * @author fschoeppl
 *
 */
@Entity
public class SearchResponse {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(nullable = false)
	private long id;
	
	private int progress;
	
	private String query;
	
	private String filters;
	
	@Transient
	private List<SearchEntry> files;
	
	@Transient
	private List<CountedEntry> bySource;
	
	@Transient
	private List<CountedEntry> byType;
	
	@Transient
	private List<CountedEntry> byJob;
	
	public SearchResponse() { }
	
	public SearchResponse(String query) {
		this(query, new ArrayList<String>());
	}
	
	static String join(Collection<?> s, String delimiter) {
    StringBuilder builder = new StringBuilder();
    Iterator<?> iter = s.iterator();
    while (iter.hasNext()) {
        builder.append(iter.next());
        if (!iter.hasNext()) {
          break;                  
        }
        builder.append(delimiter);
    }
    return builder.toString();
	}
	
  public SearchResponse(String query, List<String> filters) {
		this.query = query;
		this.setFilters(join(filters, ","));
	}	
	
	public SearchResponse(long id, int status, String query, List<SearchEntry> files) {
		this(id, status, query, files, null, null, null);
	}
	
	public SearchResponse(long id, int status, String query, List<CountedEntry> bySource, List<CountedEntry> byType, List<CountedEntry> byJob) {
		this(id, status, query, null, bySource, byType, byJob);
	}
	
	public SearchResponse(long id, int status, String query, List<SearchEntry> files, List<CountedEntry> bySource, List<CountedEntry> byType, List<CountedEntry> byJob) {
		this.id = id;
		this.progress = status;
		this.files = files;
		this.bySource = bySource;
		this.byType = byType;
		this.byJob = byJob;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public List<CountedEntry> getBySource() {
		return bySource;
	}

	public void setBySource(List<CountedEntry> bySource) {
		this.bySource = bySource;
	}

	public List<CountedEntry> getByType() {
		return byType;
	}

	public void setByType(List<CountedEntry> byType) {
		this.byType = byType;
	}
	
	public List<CountedEntry> getByJob() {
		return byJob;
	}

	public void setByJob(List<CountedEntry> byJob) {
		this.byJob = byJob;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public List<SearchEntry> getFiles() {
		return files;
	}

	public void setFiles(List<SearchEntry> files) {
		this.files = files;
	} 
	
	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public static class CountedEntry {
		private String title;
		private int count;
		
		public CountedEntry() {
		}
		
		public CountedEntry(String title, int count) {
			if (title.contains(";"))
				this.title = title.substring(0, title.indexOf(";"));
			else
				this.title = title;
			this.count = count;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
	}

	public static class SearchEntry {
		private String fileId;
		private Date timeStamp;
		private String title;
		private String type;
		private String thumbnailUrl;
		private String datasource;
		private Long datasourceId;
		private String jobName;
		private String preview;
		private Map<String, String> properties = new HashMap<String, String>();
		
		public SearchEntry() {
			
		}
		
		public SearchEntry(String fileId, Date timeStamp, String type, String title,
				String thumbnailUrl, String datasource, String jobName) {
			this.fileId = fileId;
			this.timeStamp = timeStamp;
			this.title = title;
			this.setType(type);
			this.thumbnailUrl = thumbnailUrl;
			this.datasource = datasource;
			this.jobName = jobName;
		}
		
		public String getProperty(String key) {
			return properties.get(key);
		}
		
		public void setProperty(String key, String value) {
			properties.put(key, value);
		}
		
		public Set<String> getPropertyKeys() {
			return properties.keySet();
		}
		
		public Date getTimeStamp() {
			return timeStamp;
		}
		public void setTimeStamp(Date timeStamp) {
			this.timeStamp = timeStamp;
		} 
		public String getTitle() {
			return title;
		}
		public void setTitle(String filename) {
			this.title = filename;
		}
		public String getThumbnailUrl() {
			return thumbnailUrl;
		}
		public void setThumbnailUrl(String thumbnailUrl) {
			this.thumbnailUrl = thumbnailUrl;
		}

		public String getFileId() {
			return fileId;
		}

		public void setFileId(String id) {
			this.fileId = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDatasource() {
			return datasource;
		}

		public void setDatasource(String datasource) {
			this.datasource = datasource;
		}
		
		public String getPreviewSnippet() {
			return preview;
		}

		public void setPreviewSnippet(String preview) {
			this.preview = preview;
		}

		public String getJobName() {
			return jobName;
		}

		public void setJobName(String jobName) {
			this.jobName = jobName;
		}

		public Long getDatasourceId() {
			return datasourceId;
		}

		public void setDatasourceId(Long datasourceId) {
			this.datasourceId = datasourceId;
		}
		
	}
}
