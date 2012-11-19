package org.backmeup.model.dto;

import java.util.HashMap;
import java.util.Map;

public class ActionProfileEntry {
  private String id;
  private Map<String, String> options = new HashMap<String, String>();
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Map<String, String> getOptions() {
    return options;
  }
  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
