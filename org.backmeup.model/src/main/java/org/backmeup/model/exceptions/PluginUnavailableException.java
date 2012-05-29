package org.backmeup.model.exceptions;

public class PluginUnavailableException extends PluginException {
  public PluginUnavailableException(String pluginId) {
    this(pluginId, null);
  }
  
  public PluginUnavailableException(String pluginId, Throwable cause) {
    super(pluginId, String.format("Plugin %s not available", pluginId), cause);
  }

  private static final long serialVersionUID = 1L;

}
