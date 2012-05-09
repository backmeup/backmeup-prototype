package org.backmeup.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * The ProfileEntry class is a key/value pair
 * that will be used by the Profile class to store
 * Profile specific information like access keys.
 * 
 * @author fschoeppl
 *
 */
@Entity
public class ProfileEntry {
	@Id
	@GeneratedValue
	private Long profileEntryId;
	private String key;
	@Column(length=4096)
	private String value;
	
	public ProfileEntry() {
	}
	
	public ProfileEntry(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		if (this.key != null)
			return this.key.hashCode();
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProfileEntry) {
			ProfileEntry e = (ProfileEntry) obj;
			if (e.key != null && key != null)
				return e.key.equals(key);
		}
		return super.equals(obj);
	}

	public Long getProfileEntryId() {
		return profileEntryId;
	}

	public void setProfileEntryId(Long profileEntryId) {
		this.profileEntryId = profileEntryId;
	}
}
