package org.backmeup.rest.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MetadataContainer {
  private Map<String, String> metadata = new HashMap<String, String>();

  public MetadataContainer(){
  }
  
  public MetadataContainer(Properties props) {
    for (Object key : props.keySet()) {
      String keyStr = (String) key;
      String valueStr = (String) props.get(key);
      setProperty(keyStr, valueStr);
    }
  }
  
  public void setProperty(String key, String value) {
    this.metadata.put(key, value);
  }
  
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
  
  

}
