package org.backmeup.dummy;

import java.util.Properties;

import org.backmeup.plugin.spi.OAuthBased;

public class DummyAuthenticator implements OAuthBased {

  @Override
  public AuthorizationType getAuthType() {
    return AuthorizationType.OAuth;
  }

  @Override
  public void postAuthorize(Properties inputProperties) {
    // do nothing
  }

  @Override
  public String createRedirectURL(Properties inputProperties, String callbackUrl) {
    // return anything
    return "NOT_NEEDED";
  }

}
