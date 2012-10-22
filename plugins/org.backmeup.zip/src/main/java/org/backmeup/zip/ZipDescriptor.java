package org.backmeup.zip;

import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;

public class ZipDescriptor implements SourceSinkDescribable {
  public static final String ZIP_ID = "org.backmeup.zip";

  @Override
  public String getId() {
    return ZIP_ID;
  }

  @Override
  public String getTitle() {
    return "ZIP-Download";
  }

  @Override
  public String getDescription() {
    return "Stores the backup within a zip file";
  }

  @Override
  public Properties getMetadata(Properties accessData) {
    return new Properties();
  }

  @Override
  public Type getType() {
    return Type.Sink;
  }

  @Override
  public String getImageURL() {
    return "";
  }

}
