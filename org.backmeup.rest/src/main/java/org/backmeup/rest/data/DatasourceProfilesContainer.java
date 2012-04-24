package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.Profile;

@XmlRootElement
public class DatasourceProfilesContainer {
	
	private List<InternalProfile> sourceProfiles;
	
	public DatasourceProfilesContainer() {
	}

	public DatasourceProfilesContainer(List<Profile> profiles) {
		setSourceProfiles(new ArrayList<InternalProfile>());
		for (Profile p : profiles) {
			getSourceProfiles().add(new InternalProfile(p.getProfileName(), p.getProfileId()));
		}
	}
	
	public List<InternalProfile> getSourceProfiles() {
		return sourceProfiles;
	}

	public void setSourceProfiles(List<InternalProfile> sourceProfiles) {
		this.sourceProfiles = sourceProfiles;
	}

	public static class InternalProfile {
		
		private String title;
		private long datasourceProfileId;
		
		public InternalProfile() {
		}
		
		public InternalProfile(String title, long datasourceProfileId) {
			this.title = title;
			this.datasourceProfileId = datasourceProfileId;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public long getDatasourceProfileId() {
			return datasourceProfileId;
		}
		public void setDatasourceProfileId(long datasourceProfileId) {
			this.datasourceProfileId = datasourceProfileId;
		}
		
		
	}
}
