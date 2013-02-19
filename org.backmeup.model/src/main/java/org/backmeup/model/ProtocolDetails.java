package org.backmeup.model;

import java.util.List;

/**
 * This class contains information about a certain
 * file. E.g. on which sinks is the file, when
 * has the file been uploaded. 
 * 
 * @author fschoeppl
 *
 */
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

	public static class Sink {
		private String title;
		private String timeStamp;
		private String path;
		
		public Sink() {
		}
		public Sink(String title, String timeStamp, String path) {
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
		public String getTimeStamp() {
			return timeStamp;
		}
		public void setTimeStamp(String timeStamp) {
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
		private String fileId;
		private String source;
		private Long sourceId;
		private long timeStamp;
		private String title;
		private String type;
		private String thumbnailURL;
		private String path;
    private String sink;
		
		public FileInfo() {
		}
		
		public FileInfo(String fileId, String source, long timeStamp,
				String title, String type, String thumbnailURL) {
			this.fileId = fileId;
			this.source = source;
			this.timeStamp = timeStamp;
			this.title = title;
			this.type = type;
			this.thumbnailURL = thumbnailURL;
		}
		public String getFileId() {
			return fileId;
		}
		public void setFileId(String fileId) {
			this.fileId = fileId;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public long getTimeStamp() {
			return timeStamp;
		}
		public void setTimeStamp(long timeStamp) {
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

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public void setSink(String sink) {
      this.sink = sink;
    }
    
    public String getSink() {
      return this.sink;
    }

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}
	}
}
