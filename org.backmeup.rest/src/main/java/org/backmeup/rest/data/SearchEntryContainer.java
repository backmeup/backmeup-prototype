package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.SearchResponse.SearchEntry;

@XmlRootElement
public class SearchEntryContainer {
	
	private String fileId;
	private Date timeStamp;
	private String title;
	private String type;
	private String preview;
	private String thumbnailUrl;
	private String datasource;
	private String jobName;
	
	private List<KeyValue> properties;
	
	public SearchEntryContainer(SearchEntry entry) {
		this.setFileId(entry.getFileId());
		this.setTimeStamp(entry.getTimeStamp());
		this.setTitle(entry.getTitle());
		this.setType(entry.getType());
		this.setThumbnailUrl(entry.getThumbnailUrl());
		this.setDatasource(entry.getDatasource());
		this.setPreview(entry.getPreviewSnippet());
		this.setJobName(entry.getJobName());
		this.properties = new ArrayList<SearchEntryContainer.KeyValue>();
		for (String key : entry.getPropertyKeys()) {
			this.properties.add(new KeyValue(key, entry.getProperty(key)));
		}
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
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

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}
	
	public List<KeyValue> getProperties() {
		return properties;
	}

	public void setProperties(List<KeyValue> properties) {
		this.properties = properties;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	class KeyValue {
		
		private String key;
		
		private String value;
		
		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		
		public void setKey(String key) {
			this.key = key;
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value= value;
		}
	}

}
