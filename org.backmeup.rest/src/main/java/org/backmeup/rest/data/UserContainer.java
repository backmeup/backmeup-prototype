package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;

@XmlRootElement
public class UserContainer {
	private String username;
	private String email;
	private Long userId;
	
	public UserContainer() {
	}
	
	public UserContainer(BackMeUpUser user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.userId = user.getUserId();
	}
	
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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
