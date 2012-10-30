package org.backmeup.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * The status class contains status information
 * about a certain BackupJob.
 * 
 * @author fschoeppl
 *
 */
@Entity
public class Status {
	
  @Id 
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long statusId;
  
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)  
	private BackupJob job;
  @Lob  
	private String message;
	@Column(name="typ")
	private String type;
	@Temporal(TemporalType.TIMESTAMP)	
	private Date timeStamp;
	private String progress;
	private String category;
	
	@Transient
	private Set<FileItem> files;	
	
	public Status() {
	}
	
	public Status(BackupJob job, String message, String type, String category, Date timeStamp) {
		this(job, message, type, category, timeStamp, null, null);
	}
	
	public Status(BackupJob job, String message, String type, String category, Date timeStamp, String progress, Set<FileItem> files) {
		this.job = job;
		this.message = message;
		this.type = type;
		this.timeStamp = timeStamp;
		this.progress = progress;
		this.files = files;
		this.category = category;
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
	
	public Long getStatusId() {
    return statusId;
  }

  public void setStatusId(Long statusId) {
    this.statusId = statusId;
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
