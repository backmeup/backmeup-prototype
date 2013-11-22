package org.backmeup.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
	private BackMeUpUser user;
	private String profileName;
	private String description;
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	// The username that has been used for a certain profile, e.g. the dropbox username or facebook username
	private String identification; 
	
	@Enumerated(EnumType.STRING)
	private Type sourceAndOrSink;	
	
	public Profile() {		
	}
	
	
	public Profile(BackMeUpUser user, String profileName, String desc, Type source) {
		this(null, user, profileName, desc, source);
	}

	public Profile(Long profileId, BackMeUpUser user, String profileName, String desc, Type sourceAndOrSink) {
		this.profileId = profileId;
		this.user = user;
		this.profileName = profileName;
		this.sourceAndOrSink = sourceAndOrSink;
		this.description = desc;
		this.created = new Date ();
		this.modified = this.created;
	}
	public Long getProfileId() {
		return profileId;
	}
	public void setProfileId(Long profileId) {
		this.modified = new Date ();
		this.profileId = profileId;
	}
	public BackMeUpUser getUser() {
		return user;
	}
	public void setUser(BackMeUpUser user) {
		this.modified = new Date ();
		this.user = user;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.modified = new Date ();
		this.profileName = profileName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String desc) {
		this.modified = new Date ();
		this.description = desc;
	} 
	
	public Type getType() {
		return sourceAndOrSink;
	}

	public void setType(Type sourceAndOrSink) {
		this.modified = new Date ();
		this.sourceAndOrSink = sourceAndOrSink;
	}


	public Date getCreated ()
	{
		return created;
	}
	public Date getModified ()
	{
		return modified;
	}


  public String getIdentification() {
    return identification;
  }


  public void setIdentification(String identification) {
    this.identification = identification;
  }
}
