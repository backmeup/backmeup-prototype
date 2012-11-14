package org.backmeup.rest.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.SearchResponse.SearchEntry;

@XmlRootElement
public class SearchEntryContainer {
	
	private String fileId;
	private Date timeStamp;
	private String title;
	private String type;
	private String thumbnailUrl;
	private String datasource;
	
	public SearchEntryContainer(SearchEntry entry) {
		this.setFileId(entry.getFileId());
		this.setTimeStamp(entry.getTimeStamp());
		this.setTitle(entry.getTitle());
		this.setType(entry.getType());
		this.setThumbnailUrl(entry.getThumbnailUrl());
		this.setDatasource(entry.getDatasource());
		// TODO implement properties key/value map
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

}
