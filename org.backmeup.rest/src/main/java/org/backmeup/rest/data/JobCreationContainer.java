package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackupJob;

@XmlRootElement
public class JobCreationContainer {
	private long jobId;
	public JobCreationContainer() {
		super();
	}

	public JobCreationContainer(BackupJob job) {
    this.jobId = job.getId();
  }

  public JobCreationContainer(long jobId) {
		super();
		this.jobId = jobId;
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
}
