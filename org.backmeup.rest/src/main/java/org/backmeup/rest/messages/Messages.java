package org.backmeup.rest.messages;

import org.backmeup.rest.data.ResultMessage;
import org.backmeup.rest.data.ResultMessage.Type;

public class Messages {
  public static final String DELETE_USER = "User has been deleted";
  public static final String DELETE_INDEX_FOR_USER = "Index has been deleted";
  public static final String DELETE_BACKUP = "Backup has been deleted from index";
  public static final String CHANGE_USER_USERNAME = "Account username has been changed";
  public static final String CHANGE_USER_PASSWORD = "Account password has been changed";
  public static final String CHANGE_USER_EMAIL = "Account email has been changed";
  public static final String LOGIN_USER = "User has been logged in";
  public static final String POST_USER_PROPERTY = "User property has been set";
  public static final String DELETE_USER_PROPERTY = "User property has been removed";
  public static final String DELETE_SOURCE_PROFILE = "Source profile has been deleted";
  public static final String UPDATE_SOURCE_PROFILE = "Source profile has been updated";
  public static final String POST_AUTH_SOURCE_PROFILE = "Source profile has been authorized";
  
  public static final String DELETE_SINK_PROFILE = "Sink profile has been deleted";
  public static final String POST_AUTH_SINK_PROFILE = "Sink profile has been authorized";
  public static final String DELETE_JOB = "Job has been deleted";
  
  public static final ResultMessage MSG_DELETE_USER = new ResultMessage(Type.success, DELETE_USER);
  public static final ResultMessage MSG_DELETE_INDEX_FOR_USER = new ResultMessage(Type.success, DELETE_INDEX_FOR_USER);
  public static final ResultMessage MSG_DELETE_BACKUP = new ResultMessage(Type.success, DELETE_BACKUP);
  public static final ResultMessage MSG_POST_PROPERTY = new ResultMessage(Type.success, POST_USER_PROPERTY);
  public static final ResultMessage MSG_DELETE_USER_PROPERTY = new ResultMessage(Type.success, DELETE_USER_PROPERTY);
  public static final ResultMessage MSG_DELETE_SOURCE_PROFILE = new ResultMessage(Type.success, DELETE_SOURCE_PROFILE);
  public static final ResultMessage MSG_UPDATE_SOURCE_PROFILE = new ResultMessage(Type.success, UPDATE_SOURCE_PROFILE);
  public static final ResultMessage MSG_POST_AUTH_SOURCE_PROFILE = new ResultMessage(Type.success, POST_AUTH_SOURCE_PROFILE);
  public static final ResultMessage MSG_DELETE_SINK_PROFILE = new ResultMessage(Type.success, DELETE_SINK_PROFILE);
  public static final ResultMessage MSG_POST_AUTH_SINK_PROFILE = new ResultMessage(Type.success, POST_AUTH_SINK_PROFILE);  
  public static final ResultMessage MSG_DELETE_JOB = new ResultMessage(Type.success, DELETE_JOB);
}
