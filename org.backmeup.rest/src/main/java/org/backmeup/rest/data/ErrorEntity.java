package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorEntity {
  private String errorMessage;
  private String errorType;

  public ErrorEntity() {
  }

  public ErrorEntity(String type, String msg) {
    this.setErrorMessage(msg);
    this.setErrorType(type);
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }

  public String getErrorType() {
    return this.errorType;
  }
}
