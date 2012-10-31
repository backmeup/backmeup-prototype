package org.backmeup.zip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.plugin.spi.InputBased;

public class ZipAuthenticator implements InputBased {

  @Override
  public AuthorizationType getAuthType() {
    return AuthorizationType.InputBased;
  }

  @Override
  public String postAuthorize(Properties inputProperties) {
    // Nothing to do here
    return null;
  }

  @Override
  public List<RequiredInputField> getRequiredInputFields() {
    // Empty list will do just fine
    return new ArrayList<RequiredInputField>();
  }

  @Override
  public Map<String, Type> getTypeMapping() {
    // Empty map will do just fine
    return new HashMap<String, Type>();
  }

  @Override
  public boolean isValid(Properties inputs) {
    // We don't need authentication data here, its always ok
    return true;
  }

}
