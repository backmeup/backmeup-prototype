package org.backmeup.logic.impl.helper;

public interface AutomaticAuthorization {
  // will be appended to the login
  public String getLoginContentAddition();

  public String getGrantAccessContentAddition();

  public String getLoginFormValue();

  public String getGrantAccessFormValue();

  public boolean isGrantAccessFormRequired();
}
