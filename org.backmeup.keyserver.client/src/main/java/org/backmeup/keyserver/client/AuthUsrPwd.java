package org.backmeup.keyserver.client;

public class AuthUsrPwd {
  private Long bmu_user_id;
  private String user_pwd;
  private Long bmu_service_id;
  private Long bmu_authinfo_id;
  private String ai_username;
  private String ai_pwd;

  public AuthUsrPwd() {
    super();
  }

  public AuthUsrPwd(Long bmu_user_id, String user_pwd, Long bmu_service_id,
      Long bmu_authinfo_id, String ai_username, String ai_pwd) {
    super();
    this.bmu_user_id = bmu_user_id;
    this.user_pwd = user_pwd;
    this.bmu_service_id = bmu_service_id;
    this.bmu_authinfo_id = bmu_authinfo_id;
    this.ai_username = ai_username;
    this.ai_pwd = ai_pwd;
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

  public String getAi_username() {
    return ai_username;
  }

  public void setAi_username(String ai_username) {
    this.ai_username = ai_username;
  }

  public String getAi_pwd() {
    return ai_pwd;
  }

  public void setAi_pwd(String ai_pwd) {
    this.ai_pwd = ai_pwd;
  }

}
