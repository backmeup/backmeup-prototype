package org.backmeup.model.exceptions;

/**
 * All exception that will be thrown within a Plugin 
 * must be wrapped within this class or derived from this
 * class.
 * 
 * @author fschoeppl
 *
 */
public class PluginException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  private String pluginId;

  public PluginException(String pluginId, String message, Throwable cause) {
    super(message, cause);
    this.pluginId = pluginId;
  }

  public PluginException(String pluginId, String message) {
    this(pluginId, "Plugin '" + pluginId + "': " + message, null);
  }

  public String getPluginId() {
    return pluginId;
  }
}
