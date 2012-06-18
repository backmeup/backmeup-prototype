package org.backmeup.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class FileItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long fileId;

  @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
  private Status status;

  private String thumbnailURL;
  private String title;  
  private Date timeStamp;

  public FileItem() {
  }

  public FileItem(String thumbnailURL, String title, Date timeStamp) {
    this.thumbnailURL = thumbnailURL;
    this.title = title;
    this.timeStamp = timeStamp;
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

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}