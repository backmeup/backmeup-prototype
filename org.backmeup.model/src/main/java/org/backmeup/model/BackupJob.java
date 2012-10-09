package org.backmeup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
  private BackMeUpUser user;
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
  private Set<ProfileOptions> sourceProfiles = new HashSet<ProfileOptions>();
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

  public BackupJob() {
    super();
  }

  public BackupJob(BackMeUpUser user, Set<ProfileOptions> sourceProfile,
      Profile sinkProfile, List<ActionProfile> requiredActions,
      Date start, long delay, Date created, Date modified, String jobTitle) {
    this.user = user;
    this.sourceProfiles = sourceProfile;
    this.sinkProfile = sinkProfile;
    this.requiredActions = requiredActions;
    this.start = start;
    this.delay = delay;
    
    this.created = created;
    this.modified = modified;
    this.jobTitle = jobTitle;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public BackMeUpUser getUser() {
    return user;
  }

  public void setUser(BackMeUpUser user) {
    this.user = user;
  }

  public Set<ProfileOptions> getSourceProfiles() {
    return sourceProfiles;
  }

  public void setSourceProfiles(Set<ProfileOptions> sourceProfiles) {
    this.sourceProfiles = sourceProfiles;
  }

  public Profile getSinkProfile() {
    return sinkProfile;
  }

  public void setSinkProfile(Profile sinkProfile) {
    this.sinkProfile = sinkProfile;
  }

  public List<ActionProfile> getRequiredActions() {
    return requiredActions;
  }

  public void setRequiredActions(List<ActionProfile> requiredActions) {
    this.requiredActions = requiredActions;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public Token getToken() {
    return token;
  }

  public void setToken(Token token) {
    this.token = token;
  }

	public Date getCreated ()
	{
		return created;
	}
	
	public void setCreated (Date created)
	{
		this.created = created;
	}
	
	public Date getModified ()
	{
		return modified;
	}
	
	public void setModified (Date modified)
	{
		this.modified = modified;
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
