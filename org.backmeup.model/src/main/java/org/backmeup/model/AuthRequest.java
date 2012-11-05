package org.backmeup.model;

import java.util.List;
import java.util.Map;

import org.backmeup.model.api.RequiredInputField;

/**
 * The AuthRequest class is used as a result of the BusinessLogic#preAuth method.
 * Depending on what kind of data source/sink has been registered,
 * following properties will be set:
 * - if redirectURL is not null, the client must open a browser and enter this URL
 * - if requiredInputs is not null, the client must enter all the values specified
 *   within this list. 
 *  
 * @see org.backmeup.logic.BusinessLogic#preAuth
 * @author fschoeppl
 *
 */
public class AuthRequest {
	private List<RequiredInputField> requiredInputs;
	private String redirectURL;
	private Profile profile;	
	
	public AuthRequest() {
		super();
	}
	public AuthRequest(List<RequiredInputField> requiredInputs, Map<String, String> typeMapping, String redirectURL,
			Profile profile) {
		super();
		this.requiredInputs = requiredInputs;
		this.redirectURL = redirectURL;
		this.profile = profile;
	}
	public List<RequiredInputField> getRequiredInputs() {
		return requiredInputs;
	}
	public void setRequiredInputs(List<RequiredInputField> requiredInputs) {
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
}
