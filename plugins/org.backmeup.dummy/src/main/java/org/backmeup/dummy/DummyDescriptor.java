package org.backmeup.dummy;

import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

public class DummyDescriptor implements SourceSinkDescribable{
  private static final String DUMMY_ID = "org.backmeup.dummy";
  
  @Override
  public String getId() {
    return DUMMY_ID;
  }

  @Override
  public String getTitle() {
    return "dummy";
  }

  @Override
  public String getDescription() {
    return "a plugin for testing purposes";
  }

  @Override
  public Properties getMetadata(Properties accessData) {
    Properties props = new Properties();
    props.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
    props.setProperty(Metadata.FILE_SIZE_LIMIT, "100");
    props.setProperty(Metadata.QUOTA, "50");
    props.setProperty(Metadata.QUOTA_LIMIT, "200");
    return props;
  }

  @Override
  public Type getType() {
    return Type.Both;
  }

  @Override
  public String getImageURL() {
    return "https://backmeup.at/dummy.png";
  }

}
