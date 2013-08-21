package org.backmeup.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The BackupJob class contains all necessary data to
 * perform the backup job. It must be created by the org.backmeup.job.JobManager's 
 * implementation. 
 * 
 * 
 * @author fschoeppl
 * 
 */
@Entity
public class BackupJob {
  public static enum JobStatus {
    queued,
    running,
    successful,
    error
  }
  
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
  private BackMeUpUser user;
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
  private Set<ProfileOptions> sourceProfiles = new HashSet<ProfileOptions>();
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true, mappedBy="job")
  private Set<JobProtocol> jobProtocols = new HashSet<JobProtocol>();
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
  private Profile sinkProfile;
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
  private List<ActionProfile> requiredActions = new ArrayList<ActionProfile>();
  @Temporal(TemporalType.TIMESTAMP)
  private Date start;
  private long delay;
  @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
  private Token token;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;
  @Temporal(TemporalType.TIMESTAMP)
  private Date modified;
  private String jobTitle;
  
  private String timeExpression;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date nextExecutionTime;
  private boolean reschedule;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastSuccessful;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastFailed;
  
  @Enumerated(EnumType.STRING)
  private JobStatus status;

  private boolean onHold = false;

  private UUID validScheduleID = null;

  public BackupJob() {
    super();
  }

  public BackupJob(BackMeUpUser user, Set<ProfileOptions> sourceProfile,
      Profile sinkProfile, List<ActionProfile> requiredActions,
      Date start, long delay, String jobTitle, boolean reschedule) {
    this.user = user;
    this.sourceProfiles = sourceProfile;
    this.sinkProfile = sinkProfile;
    this.requiredActions = requiredActions;
    this.start = start;
    this.delay = delay;
    
    this.created = new Date ();
    this.modified = this.created;
    this.jobTitle = jobTitle;
    this.reschedule = reschedule;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
	this.modified = new Date ();
    this.id = id;
  }

  public BackMeUpUser getUser() {
    return user;
  }

  public void setUser(BackMeUpUser user) {
	this.modified = new Date ();
    this.user = user;
  }

  public Set<ProfileOptions> getSourceProfiles() {
    return sourceProfiles;
  }

  public void setSourceProfiles(Set<ProfileOptions> sourceProfiles) {
	this.modified = new Date ();
    this.sourceProfiles = sourceProfiles;
  }

  public Profile getSinkProfile() {
    return sinkProfile;
  }

  public void setSinkProfile(Profile sinkProfile) {
	this.modified = new Date ();
    this.sinkProfile = sinkProfile;
  }

  public List<ActionProfile> getRequiredActions() {
    return requiredActions;
  }
  
  public List<ActionProfile> getSortedRequiredActions() {
    List<ActionProfile> ap = new ArrayList<ActionProfile>();
    ap.addAll(requiredActions);
    Collections.sort(ap, new Comparator<ActionProfile>() {
      @Override
      public int compare(ActionProfile o1, ActionProfile o2) {
        return o1.compareTo(o2);
      }
    });
    return ap;
  }

  public void setRequiredActions(List<ActionProfile> requiredActions) {
	this.modified = new Date ();
    this.requiredActions = requiredActions;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
	this.modified = new Date ();
    this.start = start;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
	this.modified = new Date ();
    this.delay = delay;
  }

  public Token getToken() {
    return token;
  }

  public void setToken(Token token) {
	this.modified = new Date ();
    this.token = token;
  }

	public Date getCreated ()
	{
		return created;
	}
	
	public Date getModified ()
	{
		return modified;
	}
	
	public String getJobTitle ()
	{
		return jobTitle;
	}
	
	public void setJobTitle (String jobTitle)
	{
		this.modified = new Date ();
		this.jobTitle = jobTitle;
	}
	
	public JobProtocol lastProtocol() {
	  JobProtocol last = null;
	  for (JobProtocol jp : jobProtocols) {
	    if (last == null || jp.getExecutionTime().compareTo(last.getExecutionTime()) > 0) {
	      last = jp;
	    }
	  }
	  return last;
	}

  public Date getNextExecutionTime() {
    return nextExecutionTime;
  }

  public void setNextExecutionTime(Date nextExecutionTime) {
    this.nextExecutionTime = nextExecutionTime;
  }

	public boolean isReschedule() {
		return reschedule;
	}

	public void setReschedule(boolean reschedule) {
		this.reschedule = reschedule;
	}

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public Date getLastSuccessful() {
    return lastSuccessful;
  }

  public void setLastSuccessful(Date lastSuccessful) {
    this.lastSuccessful = lastSuccessful;
  }

  public Date getLastFailed() {
    return lastFailed;
  }

  public void setLastFailed(Date lastFailed) {
    this.lastFailed = lastFailed;
  }
  
	public boolean isOnHold() {
		return onHold;
	}

	public void setOnHold(boolean onHold) {
		this.onHold = onHold;
	}

  public String getTimeExpression() {
    return timeExpression;
  }

  public void setTimeExpression(String timeExpression) {
    this.timeExpression = timeExpression;
  }

	public UUID getValidScheduleID ()
	{
		return validScheduleID;
	}
	
	public void setValidScheduleID (UUID validScheduleID)
	{
		this.validScheduleID = validScheduleID;
	}
}
