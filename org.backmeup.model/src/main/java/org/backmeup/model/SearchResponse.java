package org.backmeup.model;

import java.util.Date;
import java.util.List;

/**
 * Contains the result of a search when calling BusinessLogic#queryBackup
 * 
 * @author fschoeppl
 *
 */
public class SearchResponse {
	private long id;
	private int progress;
	private List<SearchEntry> files;
	private List<CountedEntry> bySource;
	private List<CountedEntry> byType;
	
	public SearchResponse() {
	}	
	
	public SearchResponse(long id, int status, List<SearchEntry> files) {
		this(id, status, files, null, null);
	}
	
	public SearchResponse(long id, int status, List<CountedEntry> bySource, List<CountedEntry> byType) {
		this(id, status, null, bySource, byType);
	}
	
	public SearchResponse(long id, int status, List<SearchEntry> files, List<CountedEntry> bySource, List<CountedEntry> byType) {
		this.id = id;
		this.progress = status;
		this.files = files;
		this.bySource = bySource;
		this.byType = byType;
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
	
	public static class CountedEntry {
		private String title;
		private int count;
		
		public CountedEntry() {
		}
		
		public CountedEntry(String title, int count) {
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
		private long fileId;
		private Date timeStamp;
		private String title;
		private String type;
		private String thumbnailUrl;
		
		public SearchEntry() {
			
		}
		
		public SearchEntry(long fileId, Date timeStamp, String type, String title,
				String thumbnailUrl) {
			this.fileId = fileId;
			this.timeStamp = timeStamp;
			this.title = title;
			this.setType(type);
			this.thumbnailUrl = thumbnailUrl;
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

		public long getFileId() {
			return fileId;
		}

		public void setFileId(long id) {
			this.fileId = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}
