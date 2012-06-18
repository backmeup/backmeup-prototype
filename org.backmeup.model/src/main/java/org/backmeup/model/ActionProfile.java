package org.backmeup.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ActionProfile {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;
  private String actionId;
  
  public ActionProfile() {
  }
  
  public ActionProfile(String actionId) {
    this.actionId = actionId;
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getActionId() {
    return actionId;
  }
  public void setActionId(String actionId) {
    this.actionId = actionId;
  }
}
