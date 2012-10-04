package org.backmeup.logic.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64;
import org.backmeup.configuration.Configuration;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.SearchResponseDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.dal.UserDao;
import org.backmeup.job.JobManager;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.Token;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.EmailVerificationException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.NotAnEmailAddressException;
import org.backmeup.model.exceptions.PasswordTooShortException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.UserAlreadyActivatedException;
import org.backmeup.model.exceptions.UserNotActivatedException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.actions.encryption.EncryptionDescribable;
import org.backmeup.plugin.api.actions.filesplitting.FilesplittDescribable;
import org.backmeup.plugin.api.actions.indexing.ElasticSearchIndexClient;
import org.backmeup.plugin.api.actions.indexing.IndexDescribable;
import org.backmeup.plugin.api.actions.indexing.IndexUtils;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;
import org.backmeup.utilities.mail.Mailer;

/**
 * Implements the BusinessLogic interface by delegating most operations to
 * following layers: - DataAccessLayer - JobManager - PluginLayer
 * 
 * If an error occurs within a method an exception will be thrown that must be
 * handled by the client of the business logic.
 * 
 * @author fschoeppl
 * 
 */
@ApplicationScoped
public class BusinessLogicImpl implements BusinessLogic {

  private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
  private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";
  private static final String CANNOT_COMPUTE_FREE = "org.backmeup.logic.impl.BusinessLogicImpl.CANNOT_COMPUTE_FREE";
  private static final String NOT_ENOUGH_SPACE = "org.backmeup.logic.impl.BusinessLogicImpl.NOT_ENOUGH_SPACE";
  private static final String CANNOT_COMPUTE_QUOTA = "org.backmeup.logic.impl.BusinessLogicImpl.CANNOT_COMPUTE_QUOTA";
  private static final String NO_PLUG_IN_FOUND_WITH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.NO_PLUG_IN_FOUND_WITH_ID";
  private static final String UNKNOWN_JOB_WITH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_JOB_WITH_ID";
  private static final String UNKNOWN_SOURCE_SINK = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SOURCE_SINK";
  private static final String USER_HAS_NO_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.USER_HAS_NO_PROFILE";
  private static final String SHUTTING_DOWN_BUSINESS_LOGIC = "org.backmeup.logic.impl.BusinessLogicImpl.SHUTTING_DOWN_BUSINESS_LOGIC";
  private static final String VALIDATION_OF_ACCESS_DATA_FAILED = "org.backmeup.logic.impl.BusinessLogicImpl.VALIDATION_OF_ACCESS_DATA_FAILED";
  private static final String USER_DOESNT_EXIST = "org.backmeup.logic.impl.BusinessLogicImpl.USER_DOESNT_EXIST";
  private static final String PARAMETER_NULL = "org.backmeup.logic.impl.BusinessLogicImpl.PARAMETER_NULL";
  private static final String INVALID_USER = "org.backmeup.logic.impl.BusinessLogicImpl.INVALID_USER";
  private static final String UNKNOWN_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_PROFILE";
  private static final String UNKNOWN_ACTION = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_ACTION";
  private static final String VERIFICATION_EMAIL_SUBJECT = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_SUBJECT";
  private static final String VERIFICATION_EMAIL_CONTENT = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_CONTENT";
  private static final String VERIFICATION_EMAIL_MIME_TYPE = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_MIME_TYPE";
  
  private static final String INDEX_HOST = "index.host";
  private static final String INDEX_PORT = "index.port";

  //private static final long DELAY_DAILY = 24 * 60 * 60 * 1000;
  private static final long DELAY_REALTIME = 1 * 1000;
  private static final long DELAY_DAILY = 24 * 60 * 60 * 1000;
  private static final long DELAY_WEEKLY = 24 * 60 * 60 * 1000 * 7;
  private static final long DELAY_MONTHLY = (long)(24 * 60 * 60 * 1000 * 365.242199 / 12.0);
  private static final long DELAY_YEARLY = (long)(24 * 60 * 60 * 1000 * 365.242199);

  @Inject
  private DataAccessLayer dal;
  
  @Inject
  private Keyserver keyserverClient;

  private Plugin plugins;
  private JobManager jobManager;

  @Inject
  @Named("callbackUrl")
  private String callbackUrl;

  @Inject
  @Named("minimalPasswordLength")
  private int minimalPasswordLength;

  @Inject
  private Connection conn;

  @Inject
  @Named("emailRegex")
  private String emailRegex;
  
  @Inject
  @Named("emailVerificationUrl")
  private String verificationUrl;
  

  private ResourceBundle textBundle = ResourceBundle
      .getBundle(BusinessLogicImpl.class.getSimpleName());

  public BusinessLogicImpl() {

  }

  public ProfileDao getProfileDao() {
    return dal.createProfileDao();
  }

  public UserDao getUserDao() {
    return dal.createUserDao();
  }

  public BackupJobDao getBackupJobDao() {
    return dal.createBackupJobDao();
  }
 
  private StatusDao getStatusDao() {
    return dal.createStatusDao();
  }
  
  private SearchResponseDao getSearchResponseDao() {
    return dal.createSearchResponseDao();
  }

  public BackMeUpUser getUser(String username) {
    return getUser(username, true);
  }
  
  public BackMeUpUser getUser(String username, boolean checkActivation) {
    try {
      conn.beginOrJoin();
      BackMeUpUser u = getUserDao().findByName(username);
      if (u == null)
        throw new UnknownUserException(username);
      if (checkActivation && !u.isActivated())
        throw new UserNotActivatedException(username);
      return u;
    } finally {
      conn.rollback();
    }
  }

  public BackMeUpUser deleteUser(String username) {
    conn.begin();
    try {
      BackMeUpUser u = getUser(username, false);
      UserDao userDao = getUserDao();
      
      keyserverClient.deleteUser(u.getUserId());

      BackupJobDao jobDao = getBackupJobDao();
      StatusDao statusDao = getStatusDao();
      for (BackupJob job : jobDao.findByUsername(username)) {
        for (Status status : statusDao.findByJobId(job.getId())) {
          statusDao.delete(status);
        }
        jobDao.delete(job);
      }

      ProfileDao profileDao = getProfileDao();
      for (Profile p : profileDao.findProfilesByUsername(username)) {
        profileDao.delete(p);
      }

      userDao.delete(u);      
      conn.commit();
      return u;
    } finally {
      conn.rollback();
    }
  }

  public BackMeUpUser changeUser(String oldUsername, String newUsername, String oldPassword,
      String newPassword, String newEmail) {
    try {
      conn.begin();
      BackMeUpUser u = getUser(oldUsername);
      UserDao udao = getUserDao();
      if (!oldUsername.equals(newUsername) && udao.findByName(newUsername) != null) {
        throw new AlreadyRegisteredException(newUsername);
      }
      
      if (!keyserverClient.validateUser(u.getUserId(), oldPassword)) {      
        conn.rollback();
        throw new InvalidCredentialsException();
      }      

      if (newPassword != null) {
        throwIfPasswordInvalid(newPassword);
        keyserverClient.changeUserPassword(u.getUserId(), oldPassword, newPassword);      
      }
      
      if (newEmail != null) {
        throwIfEmailInvalid(newEmail);
        u.setEmail(newEmail);
      }
      
      if (newUsername != null && !oldUsername.equals(newUsername)) {
        u.setUsername(newUsername);
      }

      // TODO: Remove keyring from change user options
      udao.save(u);
      conn.commit();
      return u;
    } finally {
      conn.rollback();
    }
  }

  public BackMeUpUser login(String username, String password) {
    try {
      conn.begin();
      BackMeUpUser u = getUser(username, false);           
      if (u == null || !keyserverClient.validateUser(u.getUserId(), password))
    	  throw new InvalidCredentialsException();
      
      return u;
    } finally {
      conn.rollback();
    }
  }

  private void throwIfPasswordInvalid(String password) {
    if (password.length() < minimalPasswordLength) {
      throw new PasswordTooShortException(minimalPasswordLength, password == null ? 0 : password.length());
    }
  }

   private void throwIfEmailInvalid(String email) {
    Pattern pattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(email);
    if (!matcher.matches()) {
      throw new NotAnEmailAddressException(emailRegex, email);
    }
  }

  public BackMeUpUser register(String username, String password,
      String keyRingPassword, String email) throws AlreadyRegisteredException,
      IllegalArgumentException {
    if (username == null || password == null || keyRingPassword == null
        || email == null) {
      throw new IllegalArgumentException(textBundle.getString(PARAMETER_NULL));
    }
    try {
      throwIfPasswordInvalid(password);      
      throwIfPasswordInvalid(keyRingPassword);            
      throwIfEmailInvalid(email);

      conn.begin();
      UserDao userDao = getUserDao();
      BackMeUpUser existingUser = userDao.findByName(username);
      if (existingUser != null) {
        throw new AlreadyRegisteredException(existingUser.getUsername());
      }
      existingUser = userDao.findByEmail(email);
      if (existingUser != null) {
        throw new AlreadyRegisteredException(existingUser.getEmail());
      }
      BackMeUpUser u = new BackMeUpUser(username, email);
      u.setActivated(false);
      generateNewVerificationKey(u, Long.toString(new Date().getTime()));
      u = userDao.save(u);
      keyserverClient.registerUser(u.getUserId(), password);
      sendVerificationEmail(u);
      conn.commit();
      return u;
    } finally {
      conn.rollback();
    }
  }

  private void sendVerificationEmail(BackMeUpUser u) {
    String verifierUrl = String.format(verificationUrl, u.getVerificationKey());     
    Mailer.send(u.getEmail(), textBundle.getString(VERIFICATION_EMAIL_SUBJECT), MessageFormat.format(textBundle.getString(VERIFICATION_EMAIL_CONTENT), verifierUrl, u.getVerificationKey()), textBundle.getString(VERIFICATION_EMAIL_MIME_TYPE));
  }
  
  private void generateNewVerificationKey(BackMeUpUser u, String additionalPart) {
    try {
      // http://stackoverflow.com/questions/4871094/generate-activation-urls-in-java-ee-6
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      String tostore = u.getUsername() + "." + additionalPart;
      md.update(tostore.getBytes("UTF-8"));
      String verificationKey = Base64.encodeBase64String(md.digest()).replaceAll("/", "_").replaceAll("\\+", "D").replaceAll("=", "A").trim(); 
      u.setVerificationKey(verificationKey);
    } catch (NoSuchAlgorithmException e) {
      throw new BackMeUpException(e);
    } catch (UnsupportedEncodingException e) {
      throw new BackMeUpException(e);
    }
  }
  
  @Override
  public void setUserProperty(String username, String key, String value) {
    try {
      conn.beginOrJoin();
      BackMeUpUser u = getUser(username);      
      u.setUserProperty(key, value);
      conn.commit();
    } finally {
      conn.rollback();
    }
    
  }

  @Override
  public void deleteUserProperty(String username, String key) {
    try {
      conn.beginOrJoin();
      BackMeUpUser u = getUser(username);           
      u.deleteUserProperty(key);
      conn.commit();
    } finally {
      conn.rollback();
    }
  }

  public List<SourceSinkDescribable> getDatasources() {
    List<SourceSinkDescribable> sources = plugins.getConnectedDatasources();
    return sources;
  }

  public List<Profile> getDatasourceProfiles(String username) {
    try {
      conn.begin();
      List<Profile> profiles = getProfileDao()
          .findDatasourceProfilesByUsername(username);
      return profiles;
    } finally {
      conn.rollback();
    }
  }

  public Profile deleteProfile(String username, Long profile) {
    try {
      conn.begin();
      ProfileDao profileDao = getProfileDao();
      Profile p = profileDao.findById(profile);
      if (p == null || !p.getUser().getUsername().equals(username)) {
        throw new IllegalArgumentException();
      }
      profileDao.delete(p);
      conn.commit();
      return p;
    } finally {
      conn.rollback();
    }
  }

  public List<String> getDatasourceOptions(String username, Long profileId,
      String keyRingPassword) {
    try {
      conn.beginOrJoin();
      getUser(username);
      ProfileDao pd = getProfileDao();
      Profile p = pd.findById(profileId);
      if (!p.getUser().getUsername().equals(username)) {
        throw new IllegalArgumentException();
      }      
      Datasource source = plugins.getDatasource(p.getDescription());
      Token t = keyserverClient.getToken(p, keyRingPassword, new Date().getTime(), false);
      AuthDataResult authData = keyserverClient.getData(t);
      Properties accessData = authData.getByProfileId(profileId);
      return source.getAvailableOptions(accessData);      
    } finally {
      conn.rollback();
    }
  }

  public void changeProfile(Long profileId, Long jobId, List<String> sourceOptions)
  {
  	  try
	  {
		  conn.beginOrJoin();
		  ProfileDao pd = getProfileDao ();
		  Profile p = pd.findById (profileId);
		  if (p == null)
		  {
			  throw new IllegalArgumentException (String.format (textBundle.getString(UNKNOWN_PROFILE), profileId));
		  }
		  
		  BackupJobDao bd = getBackupJobDao ();
		  BackupJob backupjob = bd.findById (jobId);
		  
		  Set<ProfileOptions> profileoptions = backupjob.getSourceProfiles ();
		  for (ProfileOptions option : profileoptions)
		  {
			  if (option.getProfile ().getProfileId () == p.getProfileId ())
			  {
				  String[] new_options = sourceOptions.toArray (new String[sourceOptions.size ()]);
				  option.setOptions (new_options);
			  }
		  }
		  
		  conn.commit ();
	  }
	  finally
	  {
		  conn.rollback();
	  }
  }

  public void uploadDatasourcePlugin(String filename, InputStream data) {
    // TODO Auto-generated method stub

  }

  public void deleteDatasourcePlugin(String name) {
    // TODO Auto-generated method stub

  }

  public List<SourceSinkDescribable> getDatasinks() {
    List<SourceSinkDescribable> sinks = plugins.getConnectedDatasinks();
    return sinks;
  }

  public List<Profile> getDatasinkProfiles(String username) {
    try {
      conn.begin();
      List<Profile> profiles = getProfileDao().findDatasinkProfilesByUsername(
          username);
      return profiles;
    } finally {
      conn.rollback();
    }
  }

  public void uploadDatasinkPlugin(String filename, InputStream data) {
    // TODO Auto-generated method stub

  }

  public void deleteDatasinkPlugin(String name) {
    // TODO Auto-generated method stub

  }

  public List<ActionDescribable> getActions() {
    List<ActionDescribable> actions = plugins.getActions();
    //TODO: Move all internal actions to real OSGi bundles!!    
    actions.add(new IndexDescribable());
    actions.add(new FilesplittDescribable());
    actions.add(new EncryptionDescribable());
    return actions;
  }

  public List<String> getActionOptions(String actionId)
  {
	  //TODO : Move all internal actions to real OSGi bundles!!
	  //ActionDescribable action = plugins.getActionById (actionId);
	  //return action.getAvailableOptions ();
	  
	  if (actionId.equals ("org.backmeup.indexer"))
	  {
		  return new IndexDescribable().getAvailableOptions ();
	  }
	  else if (actionId.equals ("org.backmeup.filesplitting"))
	  {
		  return new FilesplittDescribable().getAvailableOptions();
	  }
	  else if (actionId.equals ("org.backmeup.encryption"))
	  {
		  return new EncryptionDescribable().getAvailableOptions ();
	  }
	  else
	  {
		  return new LinkedList<String> ();
	  }
  }

  public void uploadActionPlugin(String filename, InputStream data) {
    // TODO Auto-generated method stub

  }

  public void deleteActionPlugin(String name) {
    // TODO Auto-generated method stub

  }

  public BackupJob createBackupJob(String username, List<Long> sourceProfiles,
      Long sinkProfileId, Map<Long, String[]> sourceOptions,
      String[] requiredActions, String timeExpression, String keyRing) {
    try {
      conn.begin();
      BackMeUpUser user = getUser(username);     

      if (!keyserverClient.validateUser(user.getUserId(), keyRing))
        throw new InvalidCredentialsException();

      Set<ProfileOptions> profiles = new HashSet<ProfileOptions>();
      if (sourceProfiles.size() == 0) {
        throw new IllegalArgumentException(
            "There must be at least one source profile to download data from!");
      }

      for (Long source : sourceProfiles) {
        Profile p = getProfileDao().findById(source);
        if (p == null)
          throw new IllegalArgumentException(String.format(
              textBundle.getString(UNKNOWN_PROFILE), source));

        if (sourceOptions != null) {
          profiles.add(new ProfileOptions(p, sourceOptions.get(source)));
        } else {
          profiles.add(new ProfileOptions(p, null));
        }
      }

      Profile sink = getProfileDao().findById(sinkProfileId);
      if (sink == null) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(UNKNOWN_PROFILE), sinkProfileId));
      }

      List<ActionProfile> actions = new ArrayList<ActionProfile>();
      if (requiredActions != null) {
        for (String action : requiredActions) {
          ActionDescribable ad = null;
          // TODO: Remove workaround for embedded action plugins
          if ("org.backmeup.filesplitting".equals(action)) {
            ad = new FilesplittDescribable();
          } else if ("org.backmeup.indexer".equals(action)) {
            ad = new IndexDescribable();
          } else if ("org.backmeup.encryption".equals(action)) {
            ad = new EncryptionDescribable();
          } else {
            ad = plugins.getActionById(action);
          }
          if (ad == null) {
            throw new IllegalArgumentException(String.format(
                textBundle.getString(UNKNOWN_ACTION), action));
          }
          actions.add(new ActionProfile(ad.getId(), ad.getPriority()));          
        }
      }           
      Collections.sort(actions);
      
      Date start = null;
      long delay = 0;
      if (timeExpression.equalsIgnoreCase("daily")) {
        start = new Date();
        delay = DELAY_DAILY;      
      } else if (timeExpression.equalsIgnoreCase("weekly")) {
        start = new Date();
        delay = DELAY_WEEKLY;
      } else if (timeExpression.equalsIgnoreCase("monthly")) {
        start = new Date();
        delay = DELAY_MONTHLY;
      } else if (timeExpression.equalsIgnoreCase("yearly")) {
        start = new Date();
        delay = DELAY_YEARLY;
      } else {
        start = new Date();
        delay = DELAY_REALTIME;
      }
      
      BackupJob job = jobManager.createBackupJob(user, profiles, sink, actions,
          start, delay, keyRing);
      conn.commit();
      return job;
    } finally {
      conn.rollback();
    }
  }

  public List<BackupJob> getJobs(String username) {
    try {
      conn.begin();
      getUser(username);
      BackupJobDao jobDao = getBackupJobDao();
      return jobDao.findByUsername(username);
    } finally {
      conn.rollback();
    }
  }

  public void deleteJob(String username, Long jobId) {
    try {
      conn.begin();
      getUser(username);
      BackupJobDao jobDao = getBackupJobDao();
      BackupJob job = jobDao.findById(jobId);
      if (job == null)
        throw new IllegalArgumentException(String.format(NO_SUCH_JOB, jobId));
      if (!job.getUser().getUsername().equals(username))
        throw new IllegalArgumentException(String.format(JOB_USER_MISSMATCH,
            jobId, username));

      jobDao.delete(job);
      conn.commit();
    } finally {
      conn.rollback();
    }
  }
  
  private List<Status> getStatusForJob(BackupJob job, Date fromDate, Date toDate) {
    try {
      conn.beginOrJoin();            
      StatusDao sd = dal.createStatusDao();
      List<Status> stats = sd.findByJob(job.getUser().getUsername(), job.getId(), fromDate, toDate);
      return stats;
    } finally {
      conn.rollback();
    }
  }

  public List<Status> getStatus(String username, Long jobId, Date fromDate,
      Date toDate) {
    try {
      conn.begin();
      getUser(username);
      BackupJobDao jobDao = getBackupJobDao();
      
      if (jobId == null) {
        List<Status> status = new ArrayList<Status>();
        List<BackupJob> jobs = jobDao.findByUsername(username);
        for (BackupJob job : jobs) {
          status.addAll(getStatusForJob(job, fromDate, toDate));
        }
        return status;
      }
      
      BackupJob job = jobDao.findById(jobId);
      if (job == null)
        throw new IllegalArgumentException(String.format(NO_SUCH_JOB, jobId));
      if (!job.getUser().getUsername().equals(username))
        throw new IllegalArgumentException(String.format(JOB_USER_MISSMATCH,
            jobId, username));
      return getStatusForJob(job, fromDate, toDate);
    } finally {
      conn.rollback();
    }
  }

  public ProtocolDetails getProtocolDetails(String username, Long fileId) {
    // TODO Auto-generated method stub
    return null;
  }

  public ProtocolOverview getProtocolOverview(String username, String duration) {
    // TODO Auto-generated method stub
    return null;
  }

  public AuthRequest preAuth(String username, String uniqueDescIdentifier,
      String profileName, String keyRing) throws PluginException,
      InvalidCredentialsException {
    Authorizable auth = plugins.getAuthorizable(uniqueDescIdentifier);
    SourceSinkDescribable desc = plugins
        .getSourceSinkById(uniqueDescIdentifier);
    org.backmeup.model.spi.SourceSinkDescribable.Type type = desc.getType();
    AuthRequest ar = new AuthRequest();
    try {
      conn.begin();
      BackMeUpUser user = getUser(username);

      if (!keyserverClient.validateUser(user.getUserId(), keyRing)) {
        conn.rollback();
        throw new InvalidCredentialsException();
      }      
      Profile profile = new Profile(getUserDao().findByName(username),
          profileName, uniqueDescIdentifier, type);
      switch (auth.getAuthType()) {
      case OAuth:
        OAuthBased oauth = plugins
            .getOAuthBasedAuthorizable(uniqueDescIdentifier);
        Properties p = new Properties();
        p.setProperty("callback", callbackUrl);
        String redirectUrl = oauth.createRedirectURL(p, callbackUrl);
        ar.setRedirectURL(redirectUrl);        
        // TODO Store all properties within keyserver & don't store them within the local database!
        
        profile = getProfileDao().save(profile);       
        if (!keyserverClient.isServiceRegistered(profile.getProfileId()))
          keyserverClient.addService(profile.getProfileId());
        keyserverClient.addAuthInfo(profile, keyRing, p);
        break;
      case InputBased:
        InputBased ibased = plugins
            .getInputBasedAuthorizable(uniqueDescIdentifier);
        ar.setRequiredInputs(ibased.getRequiredInputFields());
        Map<String, String> typeMapping = new HashMap<String, String>();
        for (String key : ibased.getTypeMapping().keySet()) {
          InputBased.Type ibType = ibased.getTypeMapping().get(key);
          typeMapping.put(key, ibType.toString());
        }
        ar.setTypeMapping(typeMapping);
        profile = getProfileDao().save(profile);
        if (!keyserverClient.isServiceRegistered(profile.getProfileId()))
          keyserverClient.addService(profile.getProfileId());
        //keyserverClient.addAuthInfo(profile, keyRing, p);
        break;
      }       
      
      conn.commit();
      ar.setProfile(profile);
      return ar;
    } finally {
      conn.rollback();
    }
  }

  public void postAuth(Long profileId, Properties props, String keyRing)
      throws PluginException, ValidationException, InvalidCredentialsException {
    try {
      if (keyRing == null)
        throw new IllegalArgumentException("keyRing-Parameter cannot be null!");
      else if (profileId == null)
        throw new IllegalArgumentException("profileId-Parameter cannot be null!");
      else if (props == null)
        throw new IllegalArgumentException("properties-Parameter cannot be null!");
      
      conn.begin();
      ProfileDao profileDao = getProfileDao();
      Profile p = profileDao.findById(profileId);

      if (!keyserverClient.isServiceRegistered(p.getProfileId()))
        keyserverClient.addService(p.getProfileId());
      if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
        Token t = keyserverClient.getToken(p, keyRing, new Date().getTime(), false);
        AuthDataResult adr = keyserverClient.getData(t);
        if (adr.getAuthinfos().length > 0) {
          props.putAll(adr.getAuthinfos()[0].getAi_data());              
        }        
      }
      
      Authorizable auth = plugins.getAuthorizable(p.getDescription());
      if (auth.getAuthType() == AuthorizationType.InputBased) {
        InputBased inputBasedService = plugins.getInputBasedAuthorizable(p
            .getDescription());
        if (inputBasedService.isValid(props)) {
          auth.postAuthorize(props);          
          profileDao.save(p);
          keyserverClient.addAuthInfo(p, keyRing, props);
          conn.commit();
          return;
        } else {
          conn.rollback();
          throw new ValidationException(ValidationExceptionType.AuthException,
              textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
        }
      } else {
        auth.postAuthorize(props);
        profileDao.save(p);
        if (keyserverClient.isAuthInformationAvailable(p, keyRing))
          keyserverClient.deleteAuthInfo(p.getProfileId());
        keyserverClient.addAuthInfo(p, keyRing, props);
        conn.commit();
      }
    } catch (PluginException pe) {
      // TODO: Log exception
      pe.printStackTrace();
    } finally {
      conn.rollback();
    }
  }

  public long searchBackup(String username, String keyRingPassword, String query) {
	  try {
		  conn.begin();
	      BackMeUpUser user = getUser(username);     
	
	      if (!keyserverClient.validateUser(user.getUserId(), keyRingPassword))
	        throw new InvalidCredentialsException();
	      
	      SearchResponse search = new SearchResponse(query);
	      SearchResponseDao searchDao = getSearchResponseDao();
	      searchDao.save(search);
	      
	      return search.getId();
	  } finally {
		  conn.rollback();
	  }
  }

  public SearchResponse queryBackup(String username, long searchId,
      String filterType, String filterValue) {
    
	  try {
	    conn.begin();
	    
	    // TODO shouldn't we verify the user?
	    
	    SearchResponse search = getSearchResponseDao().findById(searchId);
	    String query = search.getQuery();
	    
	    Configuration config = Configuration.getConfig();
	    String host = config.getProperty(INDEX_HOST);
	    int port = Integer.parseInt(config.getProperty(INDEX_PORT));
	    
	    ElasticSearchIndexClient client = new ElasticSearchIndexClient(host, port);
	    org.elasticsearch.action.search.SearchResponse esResponse = client.queryBackup(username, query);
	    search.setFiles(IndexUtils.convertSearchEntries(esResponse));
	    search.setBySource(IndexUtils.getBySource(esResponse));
	    search.setByType(IndexUtils.getByType(esResponse));
	    return search;
	  } finally {
		conn.rollback();
	  }
  }

  public DataAccessLayer getDataAccessLayer() {
    return dal;
  }

  public void setDataAccessLayer(DataAccessLayer dal) {
    this.dal = dal;
    // conn.setDataAccessLayer(dal);
  }

  public Plugin getPlugins() {
    return plugins;
  }

  @Inject
  public void setPlugins(Plugin plugins) {
    this.plugins = plugins;
    this.plugins.startup();
  }

  public void shutdown() {
    System.out.println(textBundle.getString(SHUTTING_DOWN_BUSINESS_LOGIC));
    this.jobManager.shutdown();
    this.plugins.shutdown();
  }

  public JobManager getJobManager() {
    return jobManager;
  }

  @Inject
  public void setJobManager(JobManager jobManager) {
    this.jobManager = jobManager;
    this.jobManager.start();
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }
  
  private Properties fetchAuthenticationData(Profile p, String password) {
    Token t = keyserverClient.getToken(p, password, new Date().getTime(), false);
    AuthDataResult result = keyserverClient.getData(t);
    Properties props = new Properties();
    if (result.getAuthinfos().length > 0)
      props.putAll(result.getAuthinfos()[0].getAi_data());
    return props;
  }

  @Override
  public Properties getMetadata(String username, Long profileId, String keyRing) {
    try {
      conn.beginOrJoin();
      Profile p = getProfileDao().findById(profileId);
      if (p == null) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(UNKNOWN_PROFILE), profileId));
      }
      if (!p.getUser().getUsername().equals(username)) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
      }
      SourceSinkDescribable ssd = plugins.getSourceSinkById(p.getDescription());
      if (ssd == null) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(UNKNOWN_SOURCE_SINK), p.getDescription()));
      }      
      
      
      Properties accessData = keyRing != null ? fetchAuthenticationData(p, keyRing) : null;
      Properties metadata = ssd.getMetadata(accessData); 
      return metadata;
    } finally {
      conn.rollback();
    }
  }

  //TODO: Add password parameter to get token from keyserver to validate the profile
  public ValidationNotes validateProfile(String username, Long profileId, String keyRing) {

    try {
      conn.beginOrJoin();
      Profile p = getProfileDao().findById(profileId);
      if (p == null || !p.getUser().getUsername().equals(username)) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
      }
      Validationable validator = plugins.getValidator(p.getDescription());
      Properties accessData = fetchAuthenticationData(p, keyRing);
      return validator.validate(accessData);

    } catch (PluginException pe) {
      ValidationNotes notes = new ValidationNotes();
      notes.addValidationEntry(ValidationExceptionType.Error, pe.getMessage());
      return notes;
    } finally {
      conn.rollback();
    }
  }

  //TODO: Add password parameter to get token from keyserver to validate the profile
  @Override
  public ValidationNotes validateBackupJob(String username, Long jobId, String keyRing) {
    try {
      conn.begin();
      getUser(username);

      BackupJob job = jobManager.getBackUpJob(jobId);
      if (job == null || !job.getUser().getUsername().equals(username)) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(UNKNOWN_JOB_WITH_ID), jobId));
      }

      ValidationNotes notes = new ValidationNotes();
      try {
        // plugin-level validation
        double requiredSpace = 0;
        for (ProfileOptions po : job.getSourceProfiles()) {
          // Validate source plug-in itself
          notes.getValidationEntries().addAll(
              validateProfile(username, po.getProfile().getProfileId(), keyRing)
                  .getValidationEntries());

          SourceSinkDescribable ssd = plugins.getSourceSinkById(po.getProfile()
              .getDescription());
          if (ssd == null) {
            notes.addValidationEntry(ValidationExceptionType.Error, String
                .format(textBundle.getString(NO_PLUG_IN_FOUND_WITH_ID), po
                    .getProfile().getDescription()));
          }

          Properties meta = getMetadata(username, po.getProfile()
              .getProfileId(), keyRing);
          String quota = meta.getProperty(Metadata.QUOTA);
          if (quota != null) {
            requiredSpace += Double.parseDouble(meta
                .getProperty(Metadata.QUOTA));
          } else {
            notes.addValidationEntry(ValidationExceptionType.Warning, String
                .format(textBundle.getString(CANNOT_COMPUTE_QUOTA), po
                    .getProfile().getProfileName(), po.getProfile().getDescription()));
          }
        }
        // TODO: Add required space for index and encryption
        requiredSpace *= 1.3;
        // validate sink profile
        notes.getValidationEntries().addAll(
            validateProfile(username, job.getSinkProfile().getProfileId(), keyRing)
                .getValidationEntries());

        // validate available space
        Properties meta = getMetadata(username, job.getSinkProfile()
            .getProfileId(), keyRing);
        String sinkQuota = meta.getProperty(Metadata.QUOTA);
        String sinkQuotaLimit = meta.getProperty(Metadata.QUOTA_LIMIT);
        if (sinkQuota != null && sinkQuotaLimit != null) {
          double freeSpace = Double.parseDouble(sinkQuotaLimit)
              - Double.parseDouble(sinkQuota);
          if (freeSpace < requiredSpace) {
            notes.addValidationEntry(
                ValidationExceptionType.NotEnoughSpaceException, String.format(
                    textBundle.getString(NOT_ENOUGH_SPACE), requiredSpace,
                    freeSpace, job.getSinkProfile().getProfileName(), job
                        .getSinkProfile().getDescription()));
          }
        } else {
          notes.addValidationEntry(ValidationExceptionType.Warning, String
              .format(textBundle.getString(CANNOT_COMPUTE_FREE), job
                  .getSinkProfile().getProfileName(), job.getSinkProfile()
                  .getDescription()));
        }
      } catch (BackMeUpException bme) {
        notes.addValidationEntry(ValidationExceptionType.Error,
            bme.getMessage());
      }
      return notes;
    } finally {
      conn.rollback();
    }
  }

  //TODO: Store profile data within keyserver!
  @Override
  public void addProfileEntries(Long profileId, Properties entries, String keyRing) {
    try {
      conn.begin();
      ProfileDao dao = getProfileDao();
      Profile p = dao.findById(profileId);
      if (p == null) {
        throw new IllegalArgumentException("Unknown profile " + profileId);
      }
      Properties props = new Properties();      
      if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
        props.putAll(fetchAuthenticationData(p, keyRing));        
        keyserverClient.deleteAuthInfo(p.getProfileId());
      }
      props.putAll(entries);
      keyserverClient.addAuthInfo(p, keyRing, props);
      dao.save(p);
      conn.commit();
    } finally {
      conn.rollback();
    }
  }

  @Override
  public BackMeUpUser verifyEmailAddress(String verificationKey) {
    try {
      conn.begin();
      UserDao dao = getUserDao();
      BackMeUpUser u = dao.findByVerificationKey(verificationKey);
      if (u == null)
        throw new EmailVerificationException(verificationKey);      
      
      u.setVerificationKey(null); 
      u.setActivated(true);
          
      conn.commit();
      return u;
    } finally {
      conn.rollback();
    }
  }

  @Override
  public BackMeUpUser requestNewVerificationEmail(String username) {
    try {
      conn.begin();
      // don't check the activation here
      BackMeUpUser u = getUser(username, false);
      if (u.isActivated())
        throw new UserAlreadyActivatedException(username);
      generateNewVerificationKey(u, new Date().getTime() + "");      
      conn.commit();
      sendVerificationEmail(u);
      return u;
    } finally {
      conn.rollback();
    }
  }
  
  public List<KeyserverLog> getKeysrvLogs (BackMeUpUser user)
  {
	  return keyserverClient.getLogs (user);
  }
}
