package org.backmeup.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
  private final Logger logger = LoggerFactory.getLogger(Configuration.class);	
  
  static {
    Configuration.getConfig();
  }
  
  private static Configuration instance;
  public static Configuration getConfig() {
    if (instance == null)
      instance = new Configuration();
    return instance;
  }
  
  private Configuration() { 
    loadConfiguration();
  }
  
  private Properties configuration;
  
  private void loadConfiguration() {
    InputStream is = null;
    try {
      is = new FileInputStream(new File("config/bl.properties"));
    } catch (FileNotFoundException e) { }
    
    if (is == null) {
      is = getClass().getClassLoader().getResourceAsStream("bl.properties");  
    }
    
    if (is == null) {
      throw new RuntimeException("Failed to load bl.properties (add config/bl.properties or add it to the classpath)");
    }
   
    configuration = new Properties();
    try {
      configuration.load(is);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read bl.properties! ", e);
    } finally {
      try {
        if (is != null)
          is.close();
      } catch(Exception ex) {
        logger.error("", ex);
      }
    }
  }
  
  public String getProperty(String key) {
    return configuration.getProperty(key);
  }
  
}
