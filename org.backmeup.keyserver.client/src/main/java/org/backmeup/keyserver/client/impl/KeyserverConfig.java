package org.backmeup.keyserver.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyserverConfig {
  private final Logger logger = LoggerFactory.getLogger(KeyserverConfig.class);	
	
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
        	logger.error("", e);
        }
    }
    return props;
  }
  
  @Produces
  @Named("keyserver.scheme")
  public String getKeyserverScheme() {
    return this.loadProperties().getProperty("keyserver.scheme");
  }
  
  @Produces
  @Named("keyserver.host")
  public String getKeyserverHost() {
    return this.loadProperties().getProperty("keyserver.host");
  }
  
  @Produces
  @Named("keyserver.path")
  public String getKeyserverPath() {
    return this.loadProperties().getProperty("keyserver.path");
  }
  
  @Produces
  @Named("keyserver.truststore")
  public String getTruststore() {
    return this.loadProperties().getProperty("keyserver.truststore");
  }
  
  @Produces
  @Named("keyserver.truststoreType")
  public String getTruststoreType() {
    return this.loadProperties().getProperty("keyserver.truststoreType");
  }
  
  @Produces
  @Named("keyserver.truststorePwd")
  public String getTruststorePassword() {
    return this.loadProperties().getProperty("keyserver.truststorePwd");
  }
  
  @Produces
  @Named("keyserver.keystore")
  public String getKeystore() {
    return this.loadProperties().getProperty("keyserver.keystore");
  }
  
  @Produces
  @Named("keyserver.keystoreType")
  public String getKeystoreType() {
    return this.loadProperties().getProperty("keyserver.keystoreType");
  }
  
  @Produces
  @Named("keyserver.keystorePwd")
  public String getKeystorePassword() {
    return this.loadProperties().getProperty("keyserver.keystorePwd");
  }
  
  @Produces
  @Named("keyserver.allowAllHostnames")
  public boolean getAllowAllHostnames() {
    String aah = this.loadProperties().getProperty("keyserver.allowAllHostnames");
    return aah == null ? false : aah.equals("true");
  }
}
