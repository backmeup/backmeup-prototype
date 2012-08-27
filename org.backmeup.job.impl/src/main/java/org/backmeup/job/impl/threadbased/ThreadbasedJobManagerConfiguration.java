package org.backmeup.job.impl.threadbased;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * The ThreadbasedJobManagerConfiguration provides
 * access to the job.temporaryDirectory property
 * of the bl.properties.
 * 
 * @author fschoeppl
 *
 */
public class ThreadbasedJobManagerConfiguration {
  private Properties loadProperties() {
    Properties props = new Properties();
    InputStream is = null;
    try {
      is = getClass().getClassLoader().getResourceAsStream("bl.properties");
      props.load(is);           
    } catch (Exception e) {
    } finally {
      if (is != null)
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    return props;
  }
  
  @Produces
  @Named("job.temporaryDirectory")
  public String getTemporaryDirectory() {
    return this.loadProperties().getProperty("job.temporaryDirectory");
  }
  
  @Produces
  @Named("job.backupname")
  public String getBackupName() {
    return this.loadProperties().getProperty("job.backupname");
  }
}
