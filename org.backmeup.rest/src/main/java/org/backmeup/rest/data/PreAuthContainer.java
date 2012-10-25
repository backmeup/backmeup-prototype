package org.backmeup.rest.data;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.spi.RequiredInputField;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@XmlRootElement
@JsonSerialize(include = Inclusion.NON_NULL)
public class PreAuthContainer {
	private String profileId;
	private String type;
	private List<RequiredInputField> requiredInputs;

	private Map<String, String> typeMapping;
	private String redirectURL;
	private boolean isSourceProfile;

	public PreAuthContainer() {
		super();
	}

	public PreAuthContainer(String profileId, String type,
			List<RequiredInputField> requiredInputs, Map<String, String> typeMapping,
			String redirectURL, boolean isSourceProfile) {
		super();
		this.profileId = profileId;
		this.type = type;
		this.requiredInputs = requiredInputs;
		this.redirectURL = redirectURL;
		this.typeMapping = typeMapping;
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

	// public List<Pair> getTypeMapping() {
//	@XmlJavaTypeAdapter(MapAdapter.class)
	public Map<String, String> getTypeMapping() {
		return typeMapping;
	}

	// public void setTypeMapping(List<Pair> typeMapping) {
	public void setTypeMapping(Map<String, String> typeMapping) {
		this.typeMapping = typeMapping;
	}

	public boolean isSourceProfile() {
		return isSourceProfile;
	}

	public void setSourceProfile(boolean isSourceProfile) {
		this.isSourceProfile = isSourceProfile;
	}
}
