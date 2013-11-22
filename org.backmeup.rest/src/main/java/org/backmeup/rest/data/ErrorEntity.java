package org.backmeup.rest.data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.exceptions.BackMeUpException;

@XmlRootElement
public class ErrorEntity {
  private String errorMessage;
  private String errorType;
  private String[] trace;
  private Map<String, String> additionalInfo;

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
    
    Map<String, String> properties = new HashMap<String, String>();
    for (Method m : exception.getClass().getMethods()) {
      if (m.getParameterTypes().length == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is")) &&
          !m.getDeclaringClass().equals(BackMeUpException.class) && 
          BackMeUpException.class.isAssignableFrom(m.getDeclaringClass())) {        
        try {
          Object result = m.invoke(exception, (Object)null);
          if (result != null) {
            String key = null;
            if (m.getName().startsWith("is")) {
              key = m.getName().substring("is".length());                           
            } else {
              key = m.getName().substring("get".length());
            }
            key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
            properties.put(key,  result.toString());
          }
        } catch (IllegalArgumentException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    additionalInfo = properties.size() > 0 ? properties : null;
  }
  
  public Map<String, String> getAdditionalInfo() {
    return additionalInfo;
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
