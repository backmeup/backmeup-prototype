package org.backmeup.model;

import java.util.HashSet;
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
  private User user;
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
  private Set<ProfileOptions> sourceProfiles = new HashSet<ProfileOptions>();
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
  private Profile sinkProfile;
  @OneToMany(cascade=CascadeType.ALL)
  private Set<ActionProfile> requiredActions = new HashSet<ActionProfile>();
  private String cronExpression;

  public BackupJob() {
    super();
  }

  public BackupJob(User user, Set<ProfileOptions> sourceProfile,
      Profile sinkProfile, Set<ActionProfile> requiredActions,
      String cronExpression) {
    this.user = user;
    this.sourceProfiles = sourceProfile;
    this.sinkProfile = sinkProfile;
    this.requiredActions = requiredActions;
    this.cronExpression = cronExpression;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
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

  public Set<ActionProfile> getRequiredActions() {
    return requiredActions;
  }

  public void setRequiredActions(Set<ActionProfile> requiredActions) {
    this.requiredActions = requiredActions;
  }

  public String getCronExpression() {
    return cronExpression;
  }

  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }
}
