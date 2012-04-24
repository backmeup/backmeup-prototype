package org.backmeup.model;

import java.util.Date;
import java.util.List;

//@JsonSerialize(include = Inclusion.NON_NULL)
public class ProtocolDetails {
	private FileInfo fileInfo;
	private List<Sink> sinks;
	private List<FileInfo> similar;
	
	public ProtocolDetails() {
	}

	public ProtocolDetails(FileInfo fileInfo, List<Sink> sinks,
			List<FileInfo> similar) {
		this.fileInfo = fileInfo;
		this.sinks = sinks;
		this.similar = similar;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public List<Sink> getSinks() {
		return sinks;
	}

	public void setSinks(List<Sink> sinks) {
		this.sinks = sinks;
	}

	public List<FileInfo> getSimilar() {
		return similar;
	}

	public void setSimilar(List<FileInfo> similar) {
		this.similar = similar;
	}

	//@JsonSerialize(include = Inclusion.NON_NULL)
	public static class Sink {
		private String title;
		private Date timeStamp;
		private String path;
		
		public Sink() {
		}
		public Sink(String title, Date timeStamp, String path) {
			this.title = title;
			this.timeStamp = timeStamp;
			this.path = path;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public Date getTimeStamp() {
			return timeStamp;
		}
		public void setTimeStamp(Date timeStamp) {
			this.timeStamp = timeStamp;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		} 
	}
	
//	@JsonSerialize(include = Inclusion.NON_NULL)
	public static class FileInfo {
		private Long fileId;
		private String source;
		private Date timeStamp;
		private String title;
		private String type;
		private String thumbnailURL;
		
		public FileInfo() {
		}
		
		public FileInfo(Long fileId, String source, Date timeStamp,
				String title, String type, String thumbnailURL) {
			this.fileId = fileId;
			this.source = source;
			this.timeStamp = timeStamp;
			this.title = title;
			this.type = type;
			this.thumbnailURL = thumbnailURL;
		}
		public Long getFileId() {
			return fileId;
		}
		public void setFileId(Long fileId) {
			this.fileId = fileId;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
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
		public String getThumbnailURL() {
			return thumbnailURL;
		}
		public void setThumbnailURL(String thumbnailURL) {
			this.thumbnailURL = thumbnailURL;
		}
	}
}
