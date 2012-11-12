package org.backmeup.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.backmeup.model.spi.ValidationExceptionType;

public class ValidationNotes {  
  public static final String AUTH_EXCEPTION = "Error during authentication";
  public static final String API_EXCEPTION = "Error during API call";
  public static final String ERROR = "Plugin threw an unhandled error during the validation";
  public static final String NO_VALIDATOR_AVAILABLE = "Plugin doesn't provide a validator";
  public static final String PLUGIN_UNAVAILABLE = "Plugin is not available";
  
  private List<ValidationEntry> validationNotes = new ArrayList<ValidationEntry>();
  private BackupJob job;
  
  private static Map<ValidationExceptionType, String> exceptionText;
  
  static {
    exceptionText = new HashMap<ValidationExceptionType, String>();
    exceptionText.put(ValidationExceptionType.AuthException, AUTH_EXCEPTION);    
    exceptionText.put(ValidationExceptionType.APIException, API_EXCEPTION);
    exceptionText.put(ValidationExceptionType.Error, ERROR);
    exceptionText.put(ValidationExceptionType.NoValidatorAvailable, NO_VALIDATOR_AVAILABLE);
    exceptionText.put(ValidationExceptionType.PluginUnavailable, NO_VALIDATOR_AVAILABLE);
    
  }
  
  public void addValidationEntry(ValidationExceptionType type, String pluginId, Exception cause) {
    this.validationNotes.add(new ValidationEntry(type, exceptionText.get(type), pluginId, cause));
  }
  
  public void addValidationEntry(ValidationExceptionType type, Exception e) {
    this.addValidationEntry(type, null, e);
  }
  
  public void addValidationEntry(ValidationExceptionType type, String pluginId) {
    this.addValidationEntry(type, pluginId, null);
  }
  
  public List<ValidationEntry> getValidationEntries() {
    return this.validationNotes;
  }
  
  public BackupJob getJob() {
    return job;
  }

  public void setJob(BackupJob job) {
    this.job = job;
  }

  public static class ValidationEntry {
    private ValidationExceptionType type;
    private String message;
    private String cause;
    private String stackTrace;
    private String pluginId;
    
    public ValidationEntry(ValidationExceptionType type, String message, String pluginId, Exception cause) {
      this.type = type;
      this.message = message;
      this.pluginId = pluginId;
      if (cause != null) {
        this.setCause(cause.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);    
        try {
          pw.close();
        } catch (Exception e) {}    
        try {
          sw.close();
        } catch (Exception e) {}
        this.setStackTrace(sw.toString());
      }
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

    public String getPluginId() {
      return pluginId;
    }

    public void setPluginId(String pluginId) {
      this.pluginId = pluginId;
    }

    public String getStackTrace() {
      return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
    }

    public String getCause() {
      return cause;
    }

    public void setCause(String cause) {
      this.cause = cause;
    }
  }
}
