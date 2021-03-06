package org.backmeup.keyserver.client;

public class TokenRequest {
  private Long bmu_user_id;
  private String user_pwd;
  private Long[] bmu_service_ids;
  private Long[] bmu_authinfo_ids;
  private Long backupdate;
  private boolean reusable;
  private String encryption_pwd;

  public TokenRequest() {
    super();
  }

  public TokenRequest(Long bmu_user_id, String user_pwd,
      Long[] bmu_service_ids, Long[] bmu_authinfo_ids, Long backupdate, boolean reusable, String encryptionPwd) {
    super();
    this.bmu_user_id = bmu_user_id;
    this.user_pwd = user_pwd;
    this.bmu_service_ids = bmu_service_ids;
    this.bmu_authinfo_ids = bmu_authinfo_ids;
    this.backupdate = backupdate;
    this.reusable = reusable;
    this.encryption_pwd = encryptionPwd;
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

  public Long[] getBmu_service_ids() {
    return bmu_service_ids;
  }

  public void setBmu_service_ids(Long[] bmu_service_ids) {
    this.bmu_service_ids = bmu_service_ids;
  }

  public Long[] getBmu_authinfo_ids() {
    return bmu_authinfo_ids;
  }

  public void setBmu_authinfo_ids(Long[] bmu_authinfo_ids) {
    this.bmu_authinfo_ids = bmu_authinfo_ids;
  }

  public Long getBackupdate() {
    return backupdate;
  }

  public void setBackupdate(Long backupdate) {
    this.backupdate = backupdate;
  }

  public Boolean getReusable() {
    return reusable;
  }

  public void setReusable(Boolean reusable) {
    this.reusable = reusable;
  }

  public String getEncryption_pwd() {
    return encryption_pwd;
  }

  public void setEncryption_pwd(String encryption_pwd) {
    this.encryption_pwd = encryption_pwd;
  }

}
