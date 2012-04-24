package org.backmeup.model;

public class ProfileOptions {
	private Profile profile;
	private String[] options;
	
	public ProfileOptions() {
	}
	
	public ProfileOptions(Profile profile, String[] options) {
		this.profile = profile;
		this.options = options;
	}
	
	public Profile getProfile() {
		return profile;
	}
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	public String[] getOptions() {
		return options;
	}
	public void setOptions(String[] options) {
		this.options = options;
	}
}