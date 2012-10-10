package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.FileItem;
import org.backmeup.model.Status;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@XmlRootElement
@JsonSerialize(include = Inclusion.NON_NULL)
public class StatusContainer {
	private List<JobStatus> backupStatus;

	public StatusContainer() {
	}

	public StatusContainer(List<Status> backupStatus) {
		this.backupStatus = new ArrayList<JobStatus>();
		for (Status s : backupStatus) {			
			this.backupStatus.add(new JobStatus(s.getMessage(), s.getType(), s.getCategory (), s.getTimeStamp().getTime()+"", s.getProgress(), s.getFiles(), s.getJob ().getId ()));
		}
	}

	public List<JobStatus> getBackupStatus() {
		return backupStatus;
	}

	public void setBackupStatus(List<JobStatus> backupStatus) {
		this.backupStatus = backupStatus;
	}
	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public static class JobStatus {
		private String message;
		private String type;
		private String category;
		private String timeStamp;
		private String progress;
		private Set<FileItem> files;
		private Long jobId;
		
		public JobStatus() {
		}
		
		public JobStatus(String message, String type, String category, String timeStamp, String progress, Set<FileItem> files, Long jobId) {
			this.message = message;
			this.type = type;
			this.category = category;
			this.timeStamp = timeStamp;
			this.progress = progress;
			this.files = files;
			this.jobId = jobId;
		}
		
		public String getProgress() {
			return progress;
		}

		public void setProgress(String progress) {
			this.progress = progress;
		}

		public Set<FileItem> getFiles() {
			return files;
		}

		public void setFiles(Set<FileItem> files) {
			this.files = files;
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
		public String getTimeStamp() {
			return timeStamp;
		}
		public void setTimeStamp(String timeStamp) {
			this.timeStamp = timeStamp;
		}

		public Long getJobId ()
		{
			return jobId;
		}
		public void setJobId (Long jobId)
		{
			this.jobId = jobId;
		}

		public String getCategory ()
		{
			return category;
		}

		public void setCategory (String category)
		{
			this.category = category;
		}
	}
}
