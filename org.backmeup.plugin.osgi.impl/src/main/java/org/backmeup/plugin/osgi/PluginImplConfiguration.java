package org.backmeup.plugin.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PluginImplConfiguration provides
 * the configuration values for the OSGi container. 
 * 
 * @author fschoeppl
 *
 */
public class PluginImplConfiguration {
  private Logger logger = LoggerFactory.getLogger(PluginImplConfiguration.class);
  
  private Properties loadProperties() {
    Properties props = new Properties();
    InputStream is = null;
    try {
      is = getClass().getClassLoader().getResourceAsStream("plugin.properties");
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
  @Named("osgi.deploymentDirectory")
  public File getDeploymentDirectory() {
    return new File(this.loadProperties().getProperty(
        "osgi.deploymentDirectory", "autodeploy"));
  }

  @Produces
  @Named("osgi.temporaryDirectory")
  public File getTemporaryDirectory() {
    return new File(this.loadProperties().getProperty(
        "osgi.temporaryDirectory", "osgiTmp"));
  }

  @Produces
  @Named("osgi.exportedPackages")
  public String getExportedPackages() {
    return this.loadProperties().getProperty("osgi.exportedPackages");
  }
}
