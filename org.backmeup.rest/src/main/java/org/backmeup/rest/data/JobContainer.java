package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJob.JobStatus;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;

@XmlRootElement
public class JobContainer {
	
	private UserContainer user;
	
	private Long lastBackup;
	
	private Long nextBackup;
	
	private List<Job> backupJobs;
	
	public JobContainer() {
	}

	public JobContainer(List<BackupJob> backupJobs, BackMeUpUser user) {
		this.backupJobs = new ArrayList<Job>();
		for (BackupJob j : backupJobs) {
		  Date nextExecTime = j.getNextExecutionTime();
		  Job job = new Job(j.getId(), j.getSourceProfiles(), j.getSinkProfile(), j.getStart ().getTime (), j.getCreated ().getTime (), j.getModified ().getTime (), j.getJobTitle (), j.getDelay(), j.isOnHold ());
		  job.setLastFail(j.getLastFailed() != null ? j.getLastFailed().getTime() : null);
		  job.setLastSuccessful(j.getLastSuccessful() != null ? j.getLastSuccessful().getTime() : null);
		  job.setStatus(j.getStatus());
		  job.setTimeExpression(j.getTimeExpression());
		  JobProtocol protocol = j.lastProtocol();
		  
		  if (protocol != null) {
		    job.setLastBackup(protocol.getExecutionTime().getTime());
		  }
      
		  if (nextExecTime != null && nextExecTime.getTime() > new Date().getTime()) {
		    job.setNextBackup(nextExecTime.getTime());
		  }		  
		  
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
		}
		
		
		
		this.user = new UserContainer(user);
	}
	
	public UserContainer getUser() {
		return user;
	}
	
	public void setUser(UserContainer user) {
		this.user = user;
	}
	
	public Long getLastBackup() {
		return this.lastBackup;
	}
	
	public void setLastBackup(Long lastBackup) {
		this.lastBackup = lastBackup;
	}
	
	public Long getNextBackup() {
		return this.nextBackup;
	}
	
	public void setNextBackup(Long nextBackup) {
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
		private Long backupJobId;
		private List<DatasourceProfile> datasources;
		private DatasinkProfile datasink;
		private Long startDate;
		private Long createDate;
		private Long modifyDate;
		private String jobTitle;
		private Long delay;
		private String timeExpression;
		private Long lastBackup;
		private Long nextBackup;
		private Long lastSuccessful;
		private Long lastFail;
		private JobStatus status;
		private boolean isOnHold;
		
		
		public Job() {
		}
		 
		public Job(long backupJobId, Set<ProfileOptions> datasourceIds, Profile datasinkProfile, Long startDate, Long createDate, Long modifyDate, String jobTitle, Long delay, boolean isOnHold) {
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
			this.isOnHold = isOnHold;
			
			 if (delay == BusinessLogic.DELAY_DAILY)    
		      setTimeExpression("daily");
		    else if (delay == BusinessLogic.DELAY_MONTHLY)
		      setTimeExpression("monthly");      
		    else if (delay == BusinessLogic.DELAY_WEEKLY)
		      setTimeExpression("weekly");
		    else if (delay == BusinessLogic.DELAY_YEARLY)    
		      setTimeExpression("yearly");
		    else 
		      setTimeExpression("realtime");
		}

    public Long getBackupJobId() {
      return backupJobId;
    }

    public void setBackupJobId(Long backupJobId) {
      this.backupJobId = backupJobId;
    }

    public List<DatasourceProfile> getDatasources() {
      return datasources;
    }

    public void setDatasources(List<DatasourceProfile> datasources) {
      this.datasources = datasources;
    }

    public DatasinkProfile getDatasink() {
      return datasink;
    }

    public void setDatasink(DatasinkProfile datasink) {
      this.datasink = datasink;
    }

    public Long getStartDate() {
      return startDate;
    }

    public void setStartDate(Long startDate) {
      this.startDate = startDate;
    }

    public Long getCreateDate() {
      return createDate;
    }

    public void setCreateDate(Long createDate) {
      this.createDate = createDate;
    }

    public Long getModifyDate() {
      return modifyDate;
    }

    public void setModifyDate(Long modifyDate) {
      this.modifyDate = modifyDate;
    }

    public String getJobTitle() {
      return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
      this.jobTitle = jobTitle;
    }

    public Long getDelay() {
      return delay;
    }

    public void setDelay(Long delay) {
      this.delay = delay;
    }

    public Long getLastBackup() {
      return lastBackup;
    }

    public void setLastBackup(Long lastBackup) {
      this.lastBackup = lastBackup;
    }

    public Long getNextBackup() {
      return nextBackup;
    }

    public void setNextBackup(Long nextBackup) {
      this.nextBackup = nextBackup;
    }

    public String getTimeExpression() {
      return timeExpression;
    }

    public void setTimeExpression(String timeExpression) {
      this.timeExpression = timeExpression;
    }

    public Long getLastSuccessful() {
      return lastSuccessful;
    }

    public void setLastSuccessful(Long lastSuccessful) {
      this.lastSuccessful = lastSuccessful;
    }

    public JobStatus getStatus() {
      return status;
    }

    public void setStatus(JobStatus status) {
      this.status = status;
    }

    public Long getLastFail() {
      return lastFail;
    }

    public void setLastFail(Long lastFail) {
      this.lastFail = lastFail;
    }

	public boolean isOnHold ()
	{
		return isOnHold;
	}

	public void setOnHold (boolean onHold)
	{
		this.isOnHold = onHold;
	}
	}
}
