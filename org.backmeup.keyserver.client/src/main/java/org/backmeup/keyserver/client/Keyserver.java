package org.backmeup.keyserver.client;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;

public interface Keyserver {
  // User operations
  public void registerUser(Long userId, String password);
  public boolean isUserRegistered(Long userId);
  public void deleteUser(Long userId);
  public boolean validateUser(Long userId, String password);
  public void changeUserPassword(Long userId, String oldPassword, String newPassword);
  public void changeUserKeyRing(Long userId, String oldKeyRing, String newKeyRing);
  
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
  public Token getToken(Long userId, String userPwd, Long[] services, Long[] authinfos, Long backupdate, boolean reusable, String encryptionPwd);
  public Token getToken(Profile profile, String userPwd, Long backupdate, boolean reusable, String encryptionPwd);
  public Token getToken(BackupJob job, String userPwd, Long backupdate, boolean reusable, String encryptionPwd);
  public AuthDataResult getData(Token token);
  
  // Logs
  public List<KeyserverLog> getLogs (BackMeUpUser user);
}
