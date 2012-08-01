package org.backmeup.keyserver.client;

public class AuthOAuth {
  private Long bmu_user_id;
  private String user_pwd;
  private Long bmu_service_id;
  private Long bmu_authinfo_id;
  private String ai_oauth;

  public AuthOAuth() {
    super();
  }

  public AuthOAuth(Long bmu_user_id, String user_pwd, Long bmu_service_id,
      Long bmu_authinfo_id, String ai_oauth) {
    super();
    this.bmu_user_id = bmu_user_id;
    this.user_pwd = user_pwd;
    this.bmu_service_id = bmu_service_id;
    this.bmu_authinfo_id = bmu_authinfo_id;
    this.ai_oauth = ai_oauth;
  }

  public Long getBmu_user_id() {
    return bmu_user_id;
  }

  public void setBmu_user_id(Long bmu_user_id) {
    this.bmu_user_id = bmu_user_id;
  }

  public String getUser_pwd() {
    return user_pwd;
  }

  public void setUser_pwd(String user_pwd) {
    this.user_pwd = user_pwd;
  }

  public Long getBmu_service_id() {
    return bmu_service_id;
  }

  public void setBmu_service_id(Long bmu_service_id) {
    this.bmu_service_id = bmu_service_id;
  }

  public Long getBmu_authinfo_id() {
    return bmu_authinfo_id;
  }

  public void setBmu_authinfo_id(Long bmu_authinfo_id) {
    this.bmu_authinfo_id = bmu_authinfo_id;
  }

  public String getAi_oauth() {
    return ai_oauth;
  }

  public void setAi_oauth(String ai_oauth) {
    this.ai_oauth = ai_oauth;
  }

}
