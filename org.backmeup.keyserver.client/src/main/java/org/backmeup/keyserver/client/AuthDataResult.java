package org.backmeup.keyserver.client;

import java.util.Properties;

import org.backmeup.model.Token;


public class AuthDataResult {

  private Token newToken;  
  
  public static class UserData {
    private Long bmu_user_id;
    

    public Long getBmu_user_id() {
      return bmu_user_id;
    }

    public void setBmu_user_id(Long bmu_user_id) {
      this.bmu_user_id = bmu_user_id;
    }

    public UserData() {
      super();
    }

    public UserData(Long bmu_user_id) {
      super();
      this.bmu_user_id = bmu_user_id;
    }
  }

  private UserData user;
  private AuthData[] authinfos;

  public AuthDataResult(UserData user,
      AuthData[] authinfos) {
    super();
    this.user = user;
    this.authinfos = authinfos;
  }

  public AuthDataResult() {
    super();
  }

  
  public UserData getUser() {
    return user;
  }

  public void setUser(UserData user) {
    this.user = user;
  }

  public AuthData[] getAuthinfos() {
    return authinfos;
  }

  public void setAuthinfos(AuthData[] authinfos) {
    this.authinfos = authinfos;
  }

  
  public Token getNewToken() {
    return newToken;  
  }

  public void setNewToken(Token newToken) {
    this.newToken = newToken;
  }

  public Properties getByProfileId(Long profileId) {
    for (int i = 0; i < authinfos.length; i++) {
      if (authinfos[i].getBmu_authinfo_id() == profileId) {
        return authinfos[i].getAiData();
      }
    }
    return new Properties();
  }
}
