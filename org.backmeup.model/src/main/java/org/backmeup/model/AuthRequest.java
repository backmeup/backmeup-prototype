package org.backmeup.model;

import java.util.List;
import java.util.Map;

public class AuthRequest {
	private List<String> requiredInputs;
	private Map<String, String> typeMapping;
	private String redirectURL;
	private Profile profile;	
	
	public AuthRequest() {
		super();
	}
	public AuthRequest(List<String> requiredInputs, Map<String, String> typeMapping, String redirectURL,
			Profile profile) {
		super();
		this.requiredInputs = requiredInputs;
		this.typeMapping = typeMapping;
		this.redirectURL = redirectURL;
		this.profile = profile;
	}
	public List<String> getRequiredInputs() {
		return requiredInputs;
	}
	public void setRequiredInputs(List<String> requiredInputs) {
		this.requiredInputs = requiredInputs;
	}
	public String getRedirectURL() {
		return redirectURL;
	}
	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}
	public Profile getProfile() {
		return profile;
	}
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	public Map<String, String> getTypeMapping() {
		return typeMapping;
	}
	public void setTypeMapping(Map<String, String> typeMapping) {
		this.typeMapping = typeMapping;
	}
}
