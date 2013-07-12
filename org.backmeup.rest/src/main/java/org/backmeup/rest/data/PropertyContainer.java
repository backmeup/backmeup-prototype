package org.backmeup.rest.data;

public class PropertyContainer {
  private String name;
  private String value;
  
  public PropertyContainer() {
    super();
  }
  public PropertyContainer(String name, String value) {
    super();
    this.name = name;
    this.value = value;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  
  
}
