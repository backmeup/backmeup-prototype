package org.backmeup.keyserver.client;

public class Token {
  private String token;
  private Long bmu_token_id;
  
  public Token(String token, Long bmu_token_id) {
    super();
    this.token = token;
    this.bmu_token_id = bmu_token_id;
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

  public Long getBmu_token_id() {
    return bmu_token_id;
  }

  public void setBmu_token_id(Long bmu_token_id) {
    this.bmu_token_id = bmu_token_id;
  }
}
