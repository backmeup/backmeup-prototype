package org.backmeup.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * The User class represents a user of backmeup.
 * 
 * @author fschoeppl
 *
 */
@Entity
public class BackMeUpUser {
	@Id	
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long userId;
	// TODO: Move to keyserver!!
	@Column(unique=true, nullable=false)
	private String username;
	@Column(unique=true, nullable=false)
	private String email;
	private boolean isActivated;
	private String verificationKey;
		
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY, mappedBy="user")
	private List<JobProtocol> protocols = new ArrayList<JobProtocol>();
		
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER )
	//@Fetch(value = FetchMode.SUBSELECT)
	private Set<UserProperty> properties = new HashSet<UserProperty>();
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BackMeUpUser() {
	}
	
	public BackMeUpUser(String username, String email) {
		this(null, username, email);
	}
	
	public BackMeUpUser(Long userId, String username, String email) {
		this.userId = userId;
		this.username = username;
		this.email = email;
	}
	
	private UserProperty findProperty(String key) {
	  for (UserProperty up : getUserProperties()) {
	    if (up.getKey().equals(key))
	      return up;
	  }
	  return null;
	}
	
	public void setUserProperty(String key, String value) {
	  UserProperty up = findProperty(key);
	  if (up == null) {
	    up = new UserProperty(key, value);
	    this.getUserProperties().add(up);
	  } else {
	    up.setValue(value);
	  }
	}
	
	public String getUserProperty(String key) {
	  UserProperty up = findProperty(key);
	  if (up == null)
	    return null;
	  return up.getValue();
	}
	
	public void deleteUserProperty(String key) {
	  UserProperty up = new UserProperty(key, null);
    this.getUserProperties().remove(up);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

  public String getVerificationKey() {
    return verificationKey;
  }

  public void setVerificationKey(String verificationKey) {
    this.verificationKey = verificationKey;
  }

  public boolean isActivated() {
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }
  
  public Set<UserProperty> getUserProperties() {
    return properties;
  }
  
  @Override
  public String toString() {
	  // For debug purposes
	  return username;
  }  
}
