package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackupJob;

@XmlRootElement
public class JobUpdateRequest extends JobCreationRequest {
  private Long jobId;

  public Long getJobId() {
    return jobId;
  }

  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }
}
