package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;

@XmlRootElement
public class JobContainer {
	
	private UserContainer user;
	
	private long lastBackup;
	
	private long nextBackup;
	
	private List<Job> backupJobs;
	
	public JobContainer() {
	}

	public JobContainer(List<BackupJob> backupJobs, BackMeUpUser user) {
		this.backupJobs = new ArrayList<Job>();
		for (BackupJob j : backupJobs) {
		  JobProtocol jp = j.lastProtocol();
		  long lastBackup;
		  if (jp == null)
		    lastBackup = j.getStart().getTime();
		  else 
		    lastBackup = jp.getExecutionTime().getTime();
		  long nextBackup = lastBackup + j.getDelay();
		  Job job = new Job(j.getId(), j.getSourceProfiles(), j.getSinkProfile(), j.getStart ().getTime (), j.getCreated ().getTime (), j.getModified ().getTime (), j.getJobTitle (), j.getDelay());
		  job.setLastBackup(lastBackup);
		  job.setNextBackup(nextBackup);
			this.backupJobs.add(job);
		}
		
		if (this.backupJobs.size() > 0) {
			Collections.sort(this.backupJobs, new Comparator<Job>() {
				@Override
				public int compare(Job a, Job b) {
					return (int) (a.startDate - b.startDate);
				}
			});
			
			Job lastJob = this.backupJobs.get(this.backupJobs.size() - 1);
			this.lastBackup = lastJob.startDate;
			this.nextBackup = this.lastBackup + lastJob.delay;
		}
		
		
		
		this.user = new UserContainer(user);
	}
	
	public UserContainer getUser() {
		return user;
	}
	
	public void setUser(UserContainer user) {
		this.user = user;
	}
	
	public long getLastBackup() {
		return this.lastBackup;
	}
	
	public void setLastBackup(long lastBackup) {
		this.lastBackup = lastBackup;
	}
	
	public long getNextBackup() {
		return this.nextBackup;
	}
	
	public void setNextBackup(long nextBackup) {
		this.nextBackup = nextBackup;
	}

	public List<Job> getBackupJobs() {
		return backupJobs;
	}

	public void setBackupJobs(List<Job> backupJobs) {
		this.backupJobs = backupJobs;
	}
	
	public static class DatasinkProfile {
	  private long datasinkId;
	  private String identification;
	  
	  public DatasinkProfile(long datasinkId, String identification) {
	    this.datasinkId = datasinkId;
	    this.identification = identification;
	  }
	  
	  public DatasinkProfile() {
	    
	  }
	  
    public String getIdentification() {
      return identification;
    }
    public void setIdentification(String identification) {
      this.identification = identification;
    }
    public long getDatasinkId() {
      return datasinkId;
    }
    public void setDatasinkId(long datasinkId) {
      this.datasinkId = datasinkId;
    }	  
	}
	
	public static class DatasourceProfile {
    private long datasourceId;
    private String identification;
    
    public DatasourceProfile(long datasourceId, String identification) {
      this.setDatasourceId(datasourceId);
      this.identification = identification;
    }
    
    public DatasourceProfile() {
      
    }
    
    public String getIdentification() {
      return identification;
    }
    public void setIdentification(String identification) {
      this.identification = identification;
    }

    public long getDatasourceId() {
      return datasourceId;
    }

    public void setDatasourceId(long datasourceId) {
      this.datasourceId = datasourceId;
    }
       
  }

	public static class Job {
		private long backupJobId;
		private List<DatasourceProfile> datasources;
		private DatasinkProfile datasink;
		private long startDate;
		private long createDate;
		private long modifyDate;
		private String jobTitle;
		private long delay;
		private long lastBackup;
		private long nextBackup;
		
		
		public Job() {
		}
		 
		public Job(long backupJobId, Set<ProfileOptions> datasourceIds, Profile datasinkProfile, long startDate, long createDate, long modifyDate, String jobTitle, long delay) {
			this.backupJobId = backupJobId;
			this.setDatasources(new ArrayList<DatasourceProfile>());
			for (ProfileOptions po : datasourceIds) {
				this.getDatasources().add(new DatasourceProfile(po.getProfile().getProfileId(),  po.getProfile().getIdentification()));
			}
			this.setDatasink(new DatasinkProfile(datasinkProfile.getProfileId(), datasinkProfile.getIdentification()));
			this.startDate = startDate;
			this.createDate = createDate;
			this.modifyDate = modifyDate;
			this.jobTitle = jobTitle;
			this.delay = delay;
		} 

		public long getBackupJobId() {
			return backupJobId;
		}
		public void setBackupJobId(long backupJobId) {
			this.backupJobId = backupJobId;
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
		
		public long getDelay() {
			return delay;
		}
		
		public void setDelay(long delay) {
			this.delay = delay;
		}

    public DatasinkProfile getDatasink() {
      return datasink;
    }

    public void setDatasink(DatasinkProfile datasink) {
      this.datasink = datasink;
    }

    public List<DatasourceProfile> getDatasources() {
      return datasources;
    }

    public void setDatasources(List<DatasourceProfile> datasources) {
      this.datasources = datasources;
    }

    public long getLastBackup() {
      return lastBackup;
    }

    public void setLastBackup(long lastBackup) {
      this.lastBackup = lastBackup;
    }

    public long getNextBackup() {
      return nextBackup;
    }

    public void setNextBackup(long nextBackup) {
      this.nextBackup = nextBackup;
    }
	}
}
