package org.backmeup.model.spi;

public enum ValidationExceptionType {
  /** 
   * Plugin-specific validation errors
   */
  // if the authentication fails during the validation, add an entry with AuthException type
  AuthException,
  // if the api is broken / doesn't work as expected, add the APIException type
  APIException,
  /** 
   * Core-specific validation errors
   */
  // if an exception is unhandled, the core adds the error entity 
  Error,
  // if a plugin doesn't provide a validator, the core adds the NoValidatorAvailable entity to the ValidationNotes
  NoValidatorAvailable,
  // if a plugin is not available, the core adds the PluginUnavailable entity to the ValidationNotes
  PluginUnavailable
}
