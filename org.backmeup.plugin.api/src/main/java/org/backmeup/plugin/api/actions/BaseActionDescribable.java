package org.backmeup.plugin.api.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.ActionDescribable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseActionDescribable implements ActionDescribable {
  private final Logger logger = LoggerFactory.getLogger(BaseActionDescribable.class);

  private Properties descriptionEntries;
  
  private String propertyFilename;
  
  public BaseActionDescribable(String propertyFilename) {
    this.propertyFilename = propertyFilename;
  }
  
  public BaseActionDescribable() {
    this.propertyFilename = "action.properties";
  }
  
  private Properties getDescriptionEntries() throws PluginException {
    if (descriptionEntries == null) {
      InputStream is = null;
      try {
        is = getClass().getClassLoader().getResourceAsStream(propertyFilename);
        if (is == null) {
          throw new PluginException("UNKWN", "Please provide " + propertyFilename + " for your plugins!");
        }
        descriptionEntries = new Properties();      
        descriptionEntries.load(is);        
      } catch (IOException e) {
        throw new PluginException("UNKWN", "Unable to load from " + propertyFilename + " stream!");
      } finally {
        try {
          if (is != null)
            is.close();
        } catch (Exception ex) {
        	logger.error("", ex);
        }
      }
    }
    return descriptionEntries;
  }
  
  @Override
  public String getId() {    
    return getDescriptionEntries().getProperty("actionId");
  }

  @Override
  public String getTitle() {
    return getDescriptionEntries().getProperty("actionTitle");
  }

  @Override
  public String getDescription() {
    return getDescriptionEntries().getProperty("actionDescription");
  } 

  @Override
  public int getPriority() {
    return Integer.parseInt(getDescriptionEntries().getProperty("actionPriority"));    
  }
  
  @Override
  public Properties getMetadata(Properties accessData) {
    return new Properties();
  }
  
  @Override
  public List<String> getAvailableOptions ()
  {
	  return new LinkedList<String> ();
  }

  @Override
  public String getActionVisibility() {
    return getDescriptionEntries().getProperty("actionVisibility");
  }
}
