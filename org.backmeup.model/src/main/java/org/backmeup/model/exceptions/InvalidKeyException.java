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

  public InvalidKeyException(String plugin, String keyType, String value, String configFile) {    
    super(plugin, String.format("The key %s has an invalid value %s. Make sure that you have configured %s correctly!", keyType, configFile, value));
  }
}
