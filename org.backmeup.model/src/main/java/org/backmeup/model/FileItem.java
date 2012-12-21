package org.backmeup.model;

import java.util.Date;

public class FileItem {
  private String fileId;

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

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
  
}