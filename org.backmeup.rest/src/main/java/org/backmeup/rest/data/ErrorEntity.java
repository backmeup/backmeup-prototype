package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement
public class ErrorEntity {
  private String errorMessage;
  
  public ErrorEntity() {
  }
  
  public ErrorEntity(String msg) {
    this.setErrorMessage(msg);
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
