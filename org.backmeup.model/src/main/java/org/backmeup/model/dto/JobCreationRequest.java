package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobCreationRequest {
  private String timeExpression;
  private String keyRing;
  private String jobTitle;
  private List<SourceProfileEntry> sourceProfiles = new ArrayList<SourceProfileEntry>();
  private List<ActionProfileEntry> actions = new ArrayList<ActionProfileEntry>();
  private Long sinkProfileId;
  
  public String getTimeExpression() {
    return timeExpression;
  }

  public void setTimeExpression(String timeExpression) {
    this.timeExpression = timeExpression;
  }
  
  public String getKeyRing() {
    return keyRing;
  }

  public void setKeyRing(String keyRing) {
    this.keyRing = keyRing;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public List<SourceProfileEntry> getSourceProfiles() {
    return sourceProfiles;
  }

  public void setSourceProfiles(List<SourceProfileEntry> sourceProfiles) {
    this.sourceProfiles = sourceProfiles;
  }

  public List<ActionProfileEntry> getActions() {
    return actions;
  }

  public void setActions(List<ActionProfileEntry> actions) {
    this.actions = actions;
  }

  public Long getSinkProfileId() {
    return sinkProfileId;
  }

  public void setSinkProfileId(Long sinkProfileId) {
    this.sinkProfileId = sinkProfileId;
  }
}
