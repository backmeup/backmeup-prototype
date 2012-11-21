package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;

@XmlRootElement
public class DatasinkProfilesContainer {
	
	private UserContainer user;
	
	private List<InternalProfile> sinkProfiles;
	
	public DatasinkProfilesContainer() {
	}
	
	/*
	public DatasinkProfilesContainer(List<Profile> profiles) {
		this(profiles, null);
	}
	*/

	public DatasinkProfilesContainer(List<Profile> profiles, BackMeUpUser user) {
		setSinkProfiles(new ArrayList<InternalProfile>());
		for (Profile p : profiles) {
			getSinkProfiles().add(new InternalProfile(p.getProfileName(), p.getProfileId(), p.getDescription (), p.getCreated ().getTime (), p.getModified ().getTime (), p.getIdentification()));
		}
		this.user = new UserContainer(user);
	}
	
	public UserContainer getUser() {
		return user;
	}
	
	public void setUser(UserContainer user) {
		this.user = user;
	}
	
	public List<InternalProfile> getSinkProfiles() {
		return sinkProfiles;
	}

	public void setSinkProfiles(List<InternalProfile> sourceProfiles) {
		this.sinkProfiles = sourceProfiles;
	}

	public static class InternalProfile {
		
		private String title;
		private String identification;
		private String pluginName;
		private long datasinkProfileId;
		private long createDate;
		private long modifyDate;
		
		public InternalProfile() {
		}
		
		public InternalProfile(String title, long datasinkProfileId, String pluginName, long createDate, long modifyDate, String identification) {
			this.title = title;
			this.datasinkProfileId = datasinkProfileId;
			this.pluginName = pluginName;
			this.createDate = createDate;
			this.modifyDate = modifyDate;
			this.identification = identification;
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
		
		public long getCreateDate ()
		{
			return createDate;
		}

		public void setCreateDate (long createDate)
		{
			this.createDate = createDate;
		}

		public long getModifyDate ()
		{
			return modifyDate;
		}

		public void setModifyDate (long modifyDate)
		{
			this.modifyDate = modifyDate;
		}

    public String getIdentification() {
      return identification;
    }

    public void setIdentification(String identification) {
      this.identification = identification;
    }
	}
}
