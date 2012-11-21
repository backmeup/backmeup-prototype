package org.backmeup.model.exceptions;

/**
 * If a plugins authorization/authentication fails
 * because one of its keys are invalid,
 * this Exception will be thrown.
 * 
 * @author fschoeppl
 *
 */
public class InvalidKeyException extends PluginException {
  private static final long serialVersionUID = 1L;
  private String plugin;
  private String keyType;
  private String value;
  private String configFile;

  public InvalidKeyException(String plugin, String keyType, String value, String configFile) {    
    super(plugin, "Invalid app key / secret!");
    this.plugin = plugin;
    this.keyType = keyType;
    this.value = value;
    this.configFile = configFile;
  }

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
  }

  public String getKeyType() {
    return keyType;
  }

  public void setKeyType(String keyType) {
    this.keyType = keyType;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getConfigFile() {
    return configFile;
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }
}
