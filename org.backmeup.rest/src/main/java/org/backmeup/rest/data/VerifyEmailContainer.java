package org.backmeup.rest.data;

import org.backmeup.model.BackMeUpUser;

public class VerifyEmailContainer {
  private String username;
  
  public VerifyEmailContainer(BackMeUpUser user) {    
    this.username = user.getUsername();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }  
}
