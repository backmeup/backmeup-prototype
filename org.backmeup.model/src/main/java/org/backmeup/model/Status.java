package org.backmeup.model;

import java.util.Date;
import java.util.List;

public class Status {
	private BackupJob job;
	private String message;
	private String type;
	private Date timeStamp;
	private String progress;
	private List<FileItem> files;	
	
	public Status() {
	}
	
	public Status(BackupJob job, String message, String type, Date timeStamp) {
		this(job, message, type, timeStamp, null, null);
	}
	
	public Status(BackupJob job, String message, String type, Date timeStamp, String progress, List<FileItem> files) {
		this.job = job;
		this.message = message;
		this.type = type;
		this.timeStamp = timeStamp;
		this.progress = progress;
		this.files = files;
	}
	
	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public List<FileItem> getFiles() {
		return files;
	}

	public void setFiles(List<FileItem> files) {
		this.files = files;
	}

	public BackupJob getJob() {
		return job;
	}
	public void setJob(BackupJob job) {
		this.job = job;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public static class FileItem {
		private String thumbnailURL;
		private String title;
		private Date timeStamp;
		private Long fileId;
		
		public FileItem() {
		}
		
		public FileItem(String thumbnailURL, String title, Date timeStamp,
				Long fileId) {
			this.thumbnailURL = thumbnailURL;
			this.title = title;
			this.timeStamp = timeStamp;
			this.fileId = fileId;
		}
		public String getThumbnailURL() {
			return thumbnailURL;
		}
		public void setThumbnailURL(String thumbnailURL) {
			this.thumbnailURL = thumbnailURL;
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
		public Long getFileId() {
			return fileId;
		}
		public void setFileId(Long fileId) {
			this.fileId = fileId;
		}
		
		
	}
}
