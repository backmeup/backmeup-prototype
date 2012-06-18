package org.backmeup.logic.impl.helper;


public class DropboxAutomaticAuthorizer 
  implements AutomaticAuthorization {

    @Override
    public String getLoginContentAddition() {
      return "&login_email=backmeup70@gmx.net&login_password=dropbox0r**#";
    }

    @Override
    public String getGrantAccessContentAddition() {
      return "&allow_access=Allow";
    }

    @Override
    public String getLoginFormValue() {
      return "/login";
    }

    @Override
    public String getGrantAccessFormValue() {
      return "authorize";
    }

    @Override
    public boolean isGrantAccessFormRequired() {
      return true;
    }
  
}
