package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

public class ResultMessage {
  public static enum Type {
    success    
  }
  private Type type;
  private List<String> messages;
  
  public ResultMessage(Type type) {
    this.type = type;
    this.messages = new ArrayList<String>();
  }
  
  public ResultMessage(Type type, String ...messages) {
    this.type = type;
    this.messages = new ArrayList<String>();
    for (String msg : messages) {
      this.messages.add(msg);
    }
  }

  public Type getType() {
    return type;
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  public List<String> getMessages() {
    return messages;
  }
  
  public void addMessage(String message) {    
    this.messages.add(message);    
  }
}
