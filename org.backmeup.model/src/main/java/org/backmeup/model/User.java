package org.backmeup.model;

import java.util.HashSet;
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
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long userId;
  // TODO: Move to keyserver!!
  @Column(nullable=false, unique=true)
  private String username;
  private String password;
  private String keyRing;
  @Column(nullable=false, unique=true)
  private String email;
  private boolean isActivated;  
  private String verificationKey;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  // @Fetch(value = FetchMode.SUBSELECT)
  private Set<UserProperty> properties = new HashSet<UserProperty>();

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getKeyRing() {
    return keyRing;
  }

  public void setKeyRing(String keyRing) {
    this.keyRing = keyRing;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public User() {
  }

  public User(String username, String password, String keyRing, String email) {
    this(null, username, password, keyRing, email);
  }

  public User(Long userId, String username, String password, String keyRing,
      String email) {
    this.userId = userId;
    this.username = username;
    this.password = password;
    this.keyRing = keyRing;
    this.email = email;
  }

  private UserProperty findProperty(String key) {
    for (UserProperty up : properties) {
      if (up.getKey().equals(key))
        return up;
    }
    return null;
  }

  public void setUserProperty(String key, String value) {
    UserProperty up = findProperty(key);
    if (up == null) {
      up = new UserProperty(key, value);
      this.properties.add(up);
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
    this.properties.remove(up);
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
  
  public boolean isActivated() {
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  public String getVerificationKey() {
    return verificationKey;
  }

  public void setVerificationKey(String verificationKey) {
    this.verificationKey = verificationKey;
  }
  
  @Override
  public String toString() {
	  // For debug purposes
	  return username;
  }
}
