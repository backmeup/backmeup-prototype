package org.backmeup.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.backmeup.model.spi.SourceSinkDescribable.Type;

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
	
	@Enumerated(EnumType.STRING)
	private Type sourceAndOrSink;	
	
	public Profile() {		
	}
	
	
	public Profile(User user, String profileName, String desc, Type source) {
		this(null, user, profileName, desc, source);
	}

	public Profile(Long profileId, User user, String profileName, String desc, Type sourceAndOrSink) {
		this.profileId = profileId;
		this.user = user;
		this.profileName = profileName;
		this.sourceAndOrSink = sourceAndOrSink;
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
	
	public Type getType() {
		return sourceAndOrSink;
	}

	public void setType(Type sourceAndOrSink) {
		this.sourceAndOrSink = sourceAndOrSink;
	}
}
