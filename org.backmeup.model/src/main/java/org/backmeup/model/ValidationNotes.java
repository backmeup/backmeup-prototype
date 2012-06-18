package org.backmeup.model;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.model.spi.ValidationExceptionType;

public class ValidationNotes {  
  private List<ValidationEntry> validationNotes = new ArrayList<ValidationEntry>();
  
  public void addValidationEntry(ValidationExceptionType type, String message) {
    this.validationNotes.add(new ValidationEntry(type, message));
  }
  
  public List<ValidationEntry> getValidationEntries() {
    return this.validationNotes;
  }
  
  public static class ValidationEntry {
    private ValidationExceptionType type;
    private String message;
    
    public ValidationEntry(ValidationExceptionType type, String message) {
      this.type = type;
      this.message = message;
    }
    
    public String getMessage() {
      return message;
    }
    
    public void setMessage(String message) {
      this.message = message;
    }
    
    public ValidationExceptionType getType() {
      return type;
    }
    
    public void setType(ValidationExceptionType type) {
      this.type = type;
    }
  }
}
