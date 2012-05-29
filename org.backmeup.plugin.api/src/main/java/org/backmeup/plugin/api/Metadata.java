package org.backmeup.plugin.api;

public class Metadata {
  /**
   * Valid values are: DAILY, MONTHLY, WEEKLY
   */
  public final static String BACKUP_FREQUENCY="META_BACKUP_FREQUENCY";
  
  /**
   * Datasink specific Metadata 
   */
  
  /**
   * The file size limit in MB (e.g. "100")
   */
  public final static String FILE_SIZE_LIMIT="META_FILE_SIZE_LIMIT";
  
  /**
   * The users quota that is currently stored on a service in MB (e.g. "234.55") 
   */
  public final static String QUOTA="META_QUOTA";
  
  /**
   * The maximal amount of data that can be stored on a service in MB (e.g. "2000"). 
   */
  public final static String QUOTA_LIMIT="META_QUOTA_LIMIT";
}
