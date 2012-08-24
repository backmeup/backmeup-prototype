package org.backmeup.logic.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.ServiceDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.dal.UserDao;
import org.backmeup.job.JobManager;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Service;
import org.backmeup.model.Status;
import org.backmeup.model.Token;
import org.backmeup.model.User;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;

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
  private Connection conn;

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

  public User getUser(String username) {
    try {
      conn.beginOrJoin();
      User u = getUserDao().findByName(username);
      if (u == null)
        throw new UnknownUserException(username);
      return u;
    } finally {
      conn.rollback();
    }
  }

  public User deleteUser(String username) {
    conn.begin();
    try {
      UserDao userDao = getUserDao();
      User u = userDao.findByName(username);
      if (u == null) {
        throw new IllegalArgumentException(textBundle.getString(INVALID_USER));
      }
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

  public User changeUser(String username, String oldPassword,
      String newPassword, String newKeyRing, String newEmail) {
    try {
      conn.begin();
      UserDao udao = getUserDao();
      User u = udao.findByName(username);
      if (!keyserverClient.validateUser(u.getUserId(), oldPassword)) {      
        conn.rollback();
        throw new InvalidCredentialsException();
      }      
      // TODO: Remove keyring from change user options
      keyserverClient.changeUserPassword(u.getUserId(), oldPassword, newPassword);      
      if (newEmail != null)
        u.setEmail(newEmail);
      udao.save(u);
      conn.commit();
      return u;
    } finally {
      conn.rollback();
    }
  }

  public User login(String username, String password) {
    try {
      conn.begin();
      User u = getUserDao().findByName(username);
      if (u == null || !keyserverClient.validateUser(u.getUserId(), password))
    	  throw new InvalidCredentialsException();
      
      return u;
    } finally {
      conn.rollback();
    }
  }

  public User register(String username, String password,
      String keyRingPassword, String email) throws AlreadyRegisteredException,
      IllegalArgumentException {
    if (username == null || password == null || keyRingPassword == null
        || email == null) {
      throw new IllegalArgumentException(textBundle.getString(PARAMETER_NULL));
    }
    try {
      conn.begin();
      UserDao userDao = getUserDao();
      User existingUser = userDao.findByName(username);
      if (existingUser != null) {
        throw new AlreadyRegisteredException(existingUser.getUsername());
      }
      User u = new User(username, email);
      u = userDao.save(u);
      keyserverClient.registerUser(u.getUserId(), password);
      conn.commit();
      return u;
    } finally {
      conn.rollback();
    }
  }
  
  @Override
  public void setUserProperty(String username, String key, String value) {
    try {
      conn.beginOrJoin();
      User u = getUser(username);      
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
      UserDao userDao = getUserDao();
      User u = userDao.findByName(username);      
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
    // TODO Auto-generated method stub
    return null;
  }

  public void changeProfile(Long profileId, List<String> sourceOptions) {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getActionOptions(String actionId) {
    // TODO Auto-generated method stub
    return null;
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
      User user = getUserDao().findByName(username);
      if (user == null) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(USER_DOESNT_EXIST), username));
      }

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

      Set<ActionProfile> actions = new HashSet<ActionProfile>();
      if (requiredActions != null) {
        for (String action : requiredActions) {
          ActionDescribable ad = plugins.getActionById(action);
          if (ad == null) {
            throw new IllegalArgumentException(String.format(
                textBundle.getString(UNKNOWN_ACTION), action));
          }
          actions.add(new ActionProfile(ad.getId()));
        }
      }           
      
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
      } else {
        start = new Date();
        delay = DELAY_YEARLY;
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
      UserDao userDao = getUserDao();
      User u = userDao.findByName(username);
      if (u == null)
        throw new IllegalArgumentException(String.format(
            textBundle.getString(USER_DOESNT_EXIST), username));
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

  public List<Status> getStatus(String username, Long jobId, Date fromDate,
      Date toDate) {
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
      StatusDao sd = dal.createStatusDao();
      List<Status> stats = sd.findByJob(username, jobId, fromDate, toDate);
      return stats;
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
  
  private Service getServiceModelByName(String serviceName) {
    ServiceDao serviceDao = dal.createServiceDao();
    Service serviceModel = serviceDao.findById(serviceName.hashCode());
    
    if (serviceModel == null) {
      serviceModel = new Service(new Long(serviceName.hashCode()), serviceName);
      serviceModel = serviceDao.save(serviceModel);
      if (!keyserverClient.isServiceRegistered(serviceModel.getServiceId()))
        keyserverClient.addService(serviceModel.getServiceId());
    }
    return serviceModel;
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
      UserDao userDao = getUserDao();
      User user = userDao.findByName(username);
      if (user == null) {
        conn.rollback();
        throw new IllegalArgumentException(String.format(
            textBundle.getString(USER_DOESNT_EXIST), username));
      }

      if (!keyserverClient.validateUser(user.getUserId(), keyRing)) {
        conn.rollback();
        throw new InvalidCredentialsException();
      }
      Service serviceModel = getServiceModelByName(desc.getId());
      Profile profile = new Profile(getUserDao().findByName(username),
          profileName, uniqueDescIdentifier, type, serviceModel.getServiceId());
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
      conn.begin();
      ProfileDao profileDao = getProfileDao();
      Profile p = profileDao.findById(profileId);

      Service serviceModel = getServiceModelByName(p.getDesc());
      if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
        Token t = keyserverClient.getToken(p, keyRing, new Date().getTime(), false);
        AuthDataResult adr = keyserverClient.getData(t);
        if (adr.getAuthinfos().length > 0) {
          props.putAll(adr.getAuthinfos()[0].getAi_data());              
        }        
      }
      
      Authorizable auth = plugins.getAuthorizable(p.getDesc());
      if (auth.getAuthType() == AuthorizationType.InputBased) {
        InputBased inputBasedService = plugins.getInputBasedAuthorizable(p
            .getDesc());
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
    // TODO Auto-generated method stub
    return 0;
  }

  public SearchResponse queryBackup(String username, long searchId,
      String filterType, String filterValue) {
    // TODO Auto-generated method stub
    return null;
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
      SourceSinkDescribable ssd = plugins.getSourceSinkById(p.getDesc());
      if (ssd == null) {
        throw new IllegalArgumentException(String.format(
            textBundle.getString(UNKNOWN_SOURCE_SINK), p.getDesc()));
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
      Validationable validator = plugins.getValidator(p.getDesc());
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
      UserDao userDao = getUserDao();
      User u = userDao.findByName(username);
      if (u == null)
        throw new UnknownUserException(username);

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
              .getDesc());
          if (ssd == null) {
            notes.addValidationEntry(ValidationExceptionType.Error, String
                .format(textBundle.getString(NO_PLUG_IN_FOUND_WITH_ID), po
                    .getProfile().getDesc()));
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
                    .getProfile().getProfileName(), po.getProfile().getDesc()));
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
                        .getSinkProfile().getDesc()));
          }
        } else {
          notes.addValidationEntry(ValidationExceptionType.Warning, String
              .format(textBundle.getString(CANNOT_COMPUTE_FREE), job
                  .getSinkProfile().getProfileName(), job.getSinkProfile()
                  .getDesc()));
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
}
