package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;

@XmlRootElement
public class JobContainer {
	private List<Job> backupJobs;
	
	public JobContainer() {
	}

	public JobContainer(List<BackupJob> backupJobs) {
		this.backupJobs = new ArrayList<Job>();
		for (BackupJob j : backupJobs) {
			this.backupJobs.add(new Job(j.getId(), j.getSourceProfiles(), j.getSinkProfile().getProfileId(), j.getStart ().getTime (), j.getCreated ().getTime (), j.getModified ().getTime (), j.getJobTitle ()));
		}
	}

	public List<Job> getBackupJobs() {
		return backupJobs;
	}

	public void setBackupJobs(List<Job> backupJobs) {
		this.backupJobs = backupJobs;
	}

	public static class Job {
		private long backupJobId;
		private List<Long> datasourceIds;
		private long datasinkId;
		private long startDate;
		private long createDate;
		private long modifyDate;
		private String jobTitle;
		
		
		public Job() {
		}
		 
		public Job(long backupJobId, Set<ProfileOptions> datasourceIds, long datasinkId, long startDate, long createDate, long modifyDate, String jobTitle) {
			this.backupJobId = backupJobId;
			this.datasourceIds = new ArrayList<Long>();
			for (ProfileOptions po : datasourceIds) {
				this.datasourceIds.add(po.getProfile().getProfileId());
			}
			this.datasinkId = datasinkId;
			this.startDate = startDate;
			this.createDate = createDate;
			this.modifyDate = modifyDate;
			this.jobTitle = jobTitle;
		} 

		public long getBackupJobId() {
			return backupJobId;
		}
		public void setBackupJobId(long backupJobId) {
			this.backupJobId = backupJobId;
		}
		 
		public List<Long> getDatasourceIds() {
			return datasourceIds;
		}
		public void setDatasourceIds(List<Long> datasourceIds) {
			this.datasourceIds = datasourceIds;
		}
		 
		public long getDatasinkId() {
			return datasinkId;
		}
		public void setDatasinkId(long datasinkId) {
			this.datasinkId = datasinkId;
		}

		public long getStartDate ()
		{
			return startDate;
		}
		public void setStartDate (long startDate)
		{
			this.startDate = startDate;
		}

		public long getCreateDate ()
		{
			return createDate;
		}

		public void setCreateDate (long createDate)
		{
			this.createDate = createDate;
		}

		public long getModifyDate ()
		{
			return modifyDate;
		}

		public void setModifyDate (long modifyDate)
		{
			this.modifyDate = modifyDate;
		}

		public String getJobTitle ()
		{
			return jobTitle;
		}

		public void setJobTitle (String jobTitle)
		{
			this.jobTitle = jobTitle;
		}
	}
}
