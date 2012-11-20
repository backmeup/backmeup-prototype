package org.backmeup.model.exceptions;

public class PluginUnavailableException extends PluginException {
  public PluginUnavailableException(String pluginId) {
    this(pluginId, null);
  }
  
  public PluginUnavailableException(String pluginId, Throwable cause) {
    super(pluginId, "Plugin is not available", cause);
  }

  private static final long serialVersionUID = 1L;

}
