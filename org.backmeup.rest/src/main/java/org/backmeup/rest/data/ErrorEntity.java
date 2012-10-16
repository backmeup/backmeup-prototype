package org.backmeup.rest.data;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorEntity {
  private String errorMessage;
  private String errorType;
  private String[] trace;

  public ErrorEntity() {
  }
  
  public ErrorEntity(String type, String message) {
    this.setErrorMessage(message);
    this.setErrorType(type);
  }

  public ErrorEntity(String type, Exception exception) {    
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);    
    try {
      pw.close();
    } catch (Exception e) {}    
    try {
      sw.close();
    } catch (Exception e) {}
    setTrace(sw.toString().split("\n"));
    this.setErrorMessage(exception.getMessage());
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
  
  public String[] getTrace() {
    return trace;
  }

  public void setTrace(String[] trace) {
    this.trace = trace;
  }

  public static class Trace {
  
  }
}
