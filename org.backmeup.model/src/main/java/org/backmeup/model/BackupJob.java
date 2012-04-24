package org.backmeup.model;

import java.util.List;

import org.backmeup.model.spi.ActionDescribable;

public class BackupJob {
	private Long id;
	private User user;
	private List<ProfileOptions> sourceProfiles;
	private Profile sinkProfile;
	private List<ActionDescribable> requiredActions;
	private String cronExpression;
	
	public BackupJob() {
		super();
	}
	
	public BackupJob(long id, User user, List<ProfileOptions> sourceProfile,
			Profile sinkProfile, List<ActionDescribable> requiredActions, 
			String cronExpression) {
		super();
		this.id = id;
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
	
	public List<ProfileOptions> getSourceProfiles() {
		return sourceProfiles;
	}
	public void setSourceProfiles(List<ProfileOptions> sourceProfiles) {
		this.sourceProfiles = sourceProfiles;
	}
	public Profile getSinkProfile() {
		return sinkProfile;
	}
	public void setSinkProfile(Profile sinkProfile) {
		this.sinkProfile = sinkProfile;
	}
	 
	public List<ActionDescribable> getRequiredActions() {
		return requiredActions;
	}
	public void setRequiredActions(List<ActionDescribable> requiredActions) {
		this.requiredActions = requiredActions;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
}
