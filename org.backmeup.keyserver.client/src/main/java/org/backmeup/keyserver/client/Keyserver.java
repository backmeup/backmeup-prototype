package org.backmeup.keyserver.client;

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
  public void addAuthInfo(Long userId, String userPwd, Long serviceId, Long authInfoId, String ai_username, String ai_password);
  public void addAuthInfo(Long userId, String userPwd, Long serviceId, Long authInfoId, String ai_oauth);
  public boolean isAuthInformationAvailable(Long authInfoId, Long userId, Long serviceId, String userPwd);
  public void deleteAuthInfo(Long authInfoId);
  
  // Token operations
  public Token getToken(Long userId, String userPwd, Long[] services, Long[] authinfos, Long backupdate);
  public AuthDataResult getData(Token token);
}
