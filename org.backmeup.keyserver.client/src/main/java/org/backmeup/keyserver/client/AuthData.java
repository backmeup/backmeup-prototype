package org.backmeup.keyserver.client;

public class AuthData {
  private Long bmu_authinfo_id;
  private String ai_username;
  private String ai_pwd;
  private String ai_oauth;
  private Long bmu_user_id;
  private Long bmu_service_id;

  public AuthData() {
    super();
  }

  public AuthData(Long bmu_authinfo_id, String ai_username, String ai_password,
      String ai_oauth, Long bmu_user_id, Long bmu_service_id) {
    super();
    this.bmu_authinfo_id = bmu_authinfo_id;
    this.ai_username = ai_username;
    this.ai_pwd = ai_password;
    this.ai_oauth = ai_oauth;
    this.bmu_user_id = bmu_user_id;
    this.bmu_service_id = bmu_service_id;
  }

  public Long getBmu_authinfo_id() {
    return bmu_authinfo_id;
  }

  public void setBmu_authinfo_id(Long bmu_authinfo_id) {
    this.bmu_authinfo_id = bmu_authinfo_id;
  }

  public String getAi_username() {
    return ai_username;
  }

  public void setAi_username(String ai_username) {
    this.ai_username = ai_username;
  }

  public String getAi_password() {
    return ai_pwd;
  }

  public void setAi_password(String ai_password) {
    this.ai_pwd = ai_password;
  }

  public String getAi_oauth() {
    return ai_oauth;
  }

  public void setAi_oauth(String ai_oauth) {
    this.ai_oauth = ai_oauth;
  }

  public Long getBmu_user_id() {
    return bmu_user_id;
  }

  public void setBmu_user_id(Long bmu_user_id) {
    this.bmu_user_id = bmu_user_id;
  }

  public Long getBmu_service_id() {
    return bmu_service_id;
  }

  public void setBmu_service_id(Long bmu_service_id) {
    this.bmu_service_id = bmu_service_id;
  }

}
