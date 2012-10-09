package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.Profile;

@XmlRootElement
public class DatasinkProfilesContainer {
	
	private List<InternalProfile> sinkProfiles;
	
	public DatasinkProfilesContainer() {
	}

	public DatasinkProfilesContainer(List<Profile> profiles) {
		setSinkProfiles(new ArrayList<InternalProfile>());
		for (Profile p : profiles) {
			getSinkProfiles().add(new InternalProfile(p.getProfileName(), p.getProfileId(), p.getDescription ()));
		}
	}
	
	public List<InternalProfile> getSinkProfiles() {
		return sinkProfiles;
	}

	public void setSinkProfiles(List<InternalProfile> sourceProfiles) {
		this.sinkProfiles = sourceProfiles;
	}

	public static class InternalProfile {
		
		private String title;
		private String pluginName;
		private long datasinkProfileId;
		
		public InternalProfile() {
		}
		
		public InternalProfile(String title, long datasinkProfileId, String pluginName) {
			this.title = title;
			this.datasinkProfileId = datasinkProfileId;
			this.pluginName = pluginName;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public long getDatasinkProfileId() {
			return datasinkProfileId;
		}
		public void setDatasinkProfileId(long datasinkProfileId) {
			this.datasinkProfileId = datasinkProfileId;
		}

		public String getPluginName ()
		{
			return pluginName;
		}
		public void setPluginName (String pluginName)
		{
			this.pluginName = pluginName;
		}
	}
}
