package org.backmeup.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Token {
  private String token;
  @Id  
  private Long bmu_token_id;
  
  // the next backup date to use; leave null, if the token is not reusable
  private Long backupdate;
  
  public Token(String token, Long bmu_token_id, Long backupdate) {
    super();
    this.token = token;
    this.bmu_token_id = bmu_token_id;
    this.backupdate = backupdate;
  }
  
  public Token() {
    super();
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getTokenId() {
    return bmu_token_id;
  }

  public void setTokenId(Long bmu_token_id) {
    this.bmu_token_id = bmu_token_id;
  }

  public Long getBackupdate() {
    return backupdate;
  }

  public void setBackupdate(Long backupdate) {
    this.backupdate = backupdate;
  }
}
