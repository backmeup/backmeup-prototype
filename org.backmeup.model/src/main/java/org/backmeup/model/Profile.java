package org.backmeup.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * 
 * The Profile class represents a
 * users profile for a certain datasource or datasink.
 * Necessary profile properties might be stored within 
 * the entries list.
 * 
 * The class has been annotated with JPA specific annotations.
 * 
 * @author fschoeppl
 *
 */
@Entity
public class Profile {
	@Id
	@GeneratedValue
	private Long profileId;
	@ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER, optional=false)
	private User user;
	private String profileName;
	private String desc;
	private boolean source;
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER )
	private List<ProfileEntry> entries;
	
	public Profile() {		
	}
	
	
	public Profile(User user, String profileName, String desc, boolean source) {
		this(null, user, profileName, desc, source);
	}

	public Profile(Long profileId, User user, String profileName, String desc, boolean source) {
		this.profileId = profileId;
		this.user = user;
		this.profileName = profileName;
		this.source = source;
		this.desc = desc;
		
	}
	public Long getProfileId() {
		return profileId;
	}
	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	} 
	
	public List<ProfileEntry> getEntries() {
		if (entries == null)
			entries = new ArrayList<ProfileEntry>();
		return entries;
	}
	
	public Properties getEntriesAsProperties() {
	  Properties props = new Properties();
    for (ProfileEntry pe : getEntries()) {
      props.setProperty(pe.getKey(), pe.getValue());
    }
    return props;
	}
	
	public boolean isSource() {
		return source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}
	  
	public void putEntry(String key, String value) {
		ProfileEntry pe = new ProfileEntry(key, value);
		int index = getEntries().indexOf(pe);
		if (index != -1) {
			getEntries().get(index).setValue(value);
		} else {
			getEntries().add(pe);
		}
	}
	
	public String getEntry(String key) {
		ProfileEntry pe = new ProfileEntry(key, null);
		int index = getEntries().indexOf(pe);
		if (index != -1) {
			return getEntries().get(index).getValue();
		} 
		return null;
	}
	
	public void removeEntry(String key) {
		ProfileEntry pe = new ProfileEntry(key, null);
		int index = getEntries().indexOf(pe);
		if (index != -1) {
			getEntries().remove(index);
		}
	}
	
}
