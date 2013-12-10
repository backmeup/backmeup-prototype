package org.backmeup.plugin.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metainfo {
  private final Logger logger = LoggerFactory.getLogger(Metainfo.class);	
	
  private static final String PROP_DESTINATION = "destination";
  private static final String PROP_TYPE = "type";
  private static final String PROP_SOURCE = "source";
  private static final String PROP_BACKUP_TIME = "backupedAt";
  private static final String PROP_PARENT = "parent";
  private static final String PROP_ID = "id";
  private static final String PROP_MODIFIED = "modified";
  private static final String PROP_CREATED = "created";
  private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss z";  
  private static final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
  private Properties metainfo = new Properties();
  
  
  public void setModified(Date modifiedDate) {
    metainfo.setProperty(PROP_MODIFIED, formatter.format(modifiedDate));
  }
  
  public void setBackupDate(Date backupTime) {
    metainfo.setProperty(PROP_BACKUP_TIME, formatter.format(backupTime));
  }
  
  public void setCreated(Date createdDate) {
    metainfo.setProperty(PROP_CREATED, formatter.format(createdDate));
  }
  
  public Date getCreated() {
    return parseDate(getAttribute(PROP_CREATED));
  }
  
  public Date getBackupDate() {
    return parseDate(getAttribute(PROP_BACKUP_TIME));
  }
  
  public void setSource(String source) {
    metainfo.setProperty(PROP_SOURCE, source);
  }
  
  public String getSource() {
    return metainfo.getProperty(PROP_SOURCE);
  }
  
  public void setType(String type) {
    metainfo.setProperty(PROP_TYPE, type);
  }
  
  public String getType() {
    return metainfo.getProperty(PROP_TYPE);
  }
  
  public void setDestination(String destination) {
    metainfo.setProperty(PROP_DESTINATION, destination);
  }
  
  public String getDestination() {
    return metainfo.getProperty(PROP_DESTINATION);
  }
  
  public void setId(String id) {
    metainfo.setProperty(PROP_ID, id);
  }  
  
  public void setParent(String parentId) {
    metainfo.setProperty(PROP_PARENT, parentId);
  }

  public String getParent() {
    return metainfo.getProperty(PROP_PARENT);
  }
  
  private Date parseDate(String input) {
    if (input != null) {      
      try {
        return formatter.parse(input);
      } catch (ParseException e) { 
    	  logger.error("", e);
      }
    }
    return null;
  }
  
  public Date getModified() {
    return parseDate(getAttribute(PROP_MODIFIED));
  }
  
  public String getId() {
    return getAttribute(PROP_ID);
  }
  
  public void setAttribute(String key, String value) {
    metainfo.setProperty(key, value);
  }
  
  public String getAttribute(String key) {
    return metainfo.getProperty(key);
  }
  
  public Properties getAttributes() {
    return (Properties) metainfo.clone();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Entry<Object, Object> attributes : metainfo.entrySet()) {
      sb.append(attributes.getKey()).append(" = ").append(attributes.getValue()).append("\n");
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((metainfo.getProperty(PROP_ID) == null) ? 0 : metainfo.getProperty(PROP_ID).hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Metainfo other = (Metainfo) obj;
    if (metainfo == null || metainfo.getProperty(PROP_ID) == null) {
      if (other.metainfo != null || other.metainfo.getProperty(PROP_ID) != null)
        return false;
    } else if (!metainfo.getProperty(PROP_ID).equals(other.metainfo.getProperty(PROP_ID)))
      return false;
    return true;
  }

  
  
}
