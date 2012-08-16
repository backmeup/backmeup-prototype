package org.backmeup.keyserver.client;

import java.util.HashMap;

public class AuthData {
  private Long bmu_authinfo_id;
  private Long bmu_user_id;
  private Long bmu_service_id;
  private HashMap<String, String> ai_data = new HashMap<String, String>();

  public AuthData() {
    super();
  }

  public AuthData(Long bmu_authinfo_id, Long bmu_user_id, Long bmu_service_id) {
    super();
    this.bmu_authinfo_id = bmu_authinfo_id;
    this.bmu_user_id = bmu_user_id;
    this.bmu_service_id = bmu_service_id;
  }

  public Long getBmu_authinfo_id() {
    return bmu_authinfo_id;
  }

  public void setBmu_authinfo_id(Long bmu_authinfo_id) {
    this.bmu_authinfo_id = bmu_authinfo_id;
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

  public HashMap<String, String> getAi_data() {
    return ai_data;
  }

  public void setAi_data(HashMap<String, String> ai_data) {
    this.ai_data = ai_data;
  }

}
