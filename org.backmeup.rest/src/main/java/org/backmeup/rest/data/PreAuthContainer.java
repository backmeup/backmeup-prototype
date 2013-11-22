package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.api.RequiredInputField;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@XmlRootElement
@JsonSerialize(include = Inclusion.NON_NULL)
public class PreAuthContainer {
	private String profileId;
	private String type;
	private List<RequiredInputField> fields;

	private String redirectURL;
	private boolean isSourceProfile;

	public PreAuthContainer() {
		super();
	}

	public PreAuthContainer(String profileId, String type,
			List<RequiredInputField> requiredInputs,
			String redirectURL, boolean isSourceProfile) {
		super();
		this.profileId = profileId;
		this.type = type;
		this.fields = requiredInputs;
		this.redirectURL = redirectURL;
		this.isSourceProfile = isSourceProfile;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<RequiredInputField> getRequiredInputs() {
		return fields;
	}

	public void setRequiredInputs(List<RequiredInputField> requiredInputs) {
		this.fields = requiredInputs;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public boolean isSourceProfile() {
		return isSourceProfile;
	}

	public void setSourceProfile(boolean isSourceProfile) {
		this.isSourceProfile = isSourceProfile;
	}
}
