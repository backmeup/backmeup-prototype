package org.backmeup.keyserver.client;

import java.util.Properties;

import org.backmeup.model.Profile;
import org.backmeup.model.Token;

public interface Keyserver {
  // User operations
  public void registerUser(Long userId, String password);
  public boolean isUserRegistered(Long userId);
  public void deleteUser(Long userId);
  public boolean validateUser(Long userId, String password);
  
  //Service operations
  public void addService(Long serviceId);
  public boolean isServiceRegistered(Long serviceId);
  public void deleteService(Long serviceId);
  
  //Authentication information
  public void addAuthInfo(Long userId, String userPwd, Long serviceId, Long authInfoId, Properties keyValuePairs);
  public void addAuthInfo(Profile profile, String userPwd, Properties keyValuePairs);
  public boolean isAuthInformationAvailable(Long authInfoId, Long userId, Long serviceId, String userPwd);
  public boolean isAuthInformationAvailable(Profile profile, String userPwd);
  public void deleteAuthInfo(Long authInfoId);
  
  // Token operations
  public Token getToken(Long userId, String userPwd, Long[] services, Long[] authinfos, Long backupdate, boolean reusable);
  public Token getToken(Profile profile, String userPwd, Long backupdate, boolean reusable);
  public AuthDataResult getData(Token token);
}
