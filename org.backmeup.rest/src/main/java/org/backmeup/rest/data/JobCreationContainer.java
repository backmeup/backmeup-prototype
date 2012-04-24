package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobCreationContainer {
	private long jobId;
	
		public JobCreationContainer() {
		super();
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
