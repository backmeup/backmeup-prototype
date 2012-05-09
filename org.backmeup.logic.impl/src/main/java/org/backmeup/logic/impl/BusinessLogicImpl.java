package org.backmeup.logic.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.UserDao;
import org.backmeup.job.JobManager;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.impl.util.Connection;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileEntry;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.User;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;

/**
 * Implements the BusinessLogic interface
 * by delegating most operations to following layers:
 * - DataAccessLayer
 * - JobManager
 * - PluginLayer 
 * 
 * If an error occurs within a method an exception
 * will be thrown that must be handled by the client
 * of the business logic.
 * 
 * @author fschoeppl
 *
 */
@ApplicationScoped
public class BusinessLogicImpl implements BusinessLogic {

	@Inject
	private DataAccessLayer dal;
	
	private Plugin plugins;
	private JobManager jobManager;
	
	private UserDao userDao;
	private ProfileDao profileDao;
	private EntityManagerFactory emFactory;
	
	@Inject
	@Named("callbackUrl")
	private String callbackUrl;
	
	@Inject
	private Connection conn;
	
	public BusinessLogicImpl() {
		
	}
	
	public ProfileDao getProfileDao() {
		if (profileDao == null)
			profileDao = dal.createProfileDao();
		return profileDao;
	}
	
	public UserDao getUserDao() {
		if (userDao == null)
			userDao = dal.createUserDao();
		return userDao;
	}
	
	public User getUser(String username) {
		conn.begin();
		User u = getUserDao().findByName(username);
		conn.commit();
		return u;
	}

	public User deleteUser(String username) {
		conn.begin();
		User u = getUserDao().findByName(username);		
		if  (u == null) {
			conn.rollback();
			throw new IllegalArgumentException("invalid user");			
		}
		getUserDao().delete(u);
		conn.commit();
		return u;
	}

	public User changeUser(String username, String oldPassword,
			String newPassword, String newKeyRing, String newEmail) {
		conn.begin();
		User u = getUserDao().findByName(username);
		if (!u.getPassword().equals(oldPassword)) {
			conn.rollback();
			throw new InvalidCredentialsException();
		}
		if (newPassword != null)
			u.setPassword(newPassword);
		if (newKeyRing != null)
			u.setKeyRing(newKeyRing);
		if (newEmail != null)
			u.setEmail(newEmail);
		getUserDao().save(u);
		conn.commit();
		return u;
	}

	public User login(String username, String password) {
		conn.begin();
		User u = getUserDao().findByName(username);
		if (!u.getPassword().equals(password)) {
			conn.rollback();
			throw new InvalidCredentialsException();
		}
		conn.commit();
		return u;
	}

	public User register(String username, String password, String keyRingPassword,
			String email) throws AlreadyRegisteredException,
			IllegalArgumentException {
		if (username == null || password == null || keyRingPassword == null || email == null) {
			throw new IllegalArgumentException("Parameter null");
		}
		conn.begin();
		User existingUser = getUserDao().findByName(username);
		if (existingUser != null) {
			conn.rollback();
			throw new AlreadyRegisteredException(existingUser.getUsername());
		}
		User u = new User(username, password, keyRingPassword, email);
		u = getUserDao().save(u);
		conn.commit();
		return u;
	}

	public List<SourceSinkDescribable> getDatasources() {
		List<SourceSinkDescribable> sources = plugins.getConnectedDatasources();
		return sources;
	}

	public List<Profile> getDatasourceProfiles(String username) {
		conn.begin();
		List<Profile> profiles = getProfileDao().findDatasourceProfilesByUsername(username);
		conn.commit();
		return profiles;
	}

	public Profile deleteProfile(String username, Long profile) {
		conn.begin();
		Profile p = getProfileDao().findById(profile);
		if (p == null || !p.getUser().getUsername().equals(username)) {
			conn.rollback();
			throw new IllegalArgumentException();
		}
		getProfileDao().delete(p);
		conn.commit();
		return p;
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
		// TODO Auto-generated method stub
		return null;
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

	public BackupJob createBackupJob(String username,
			List<Long> sourceProfiles, long sinkProfileId,
			Map<Long, String[]> sourceOptions, String[] requiredActions,
			String timeExpression, String keyRing) {
		conn.begin();
		User user = getUserDao().findByName(username);
		List<ProfileOptions> profiles = new ArrayList<ProfileOptions>();
		for (Long source : sourceProfiles) {
			Profile p = getProfileDao().findById(source);
			if (sourceOptions != null) {
				profiles.add(new ProfileOptions(p, sourceOptions.get(source)));
			} else {
				profiles.add(new ProfileOptions(p, null));
			}
		}
		
		Profile sink = getProfileDao().findById(sinkProfileId);
		
		List<ActionDescribable> actions = new ArrayList<ActionDescribable>();
		if (requiredActions != null) {
			for (String action : requiredActions) {
			  	ActionDescribable ad = plugins.getActionById(action);
			  	actions.add(ad);
			}
	    }
		conn.commit();
		BackupJob job = jobManager.createBackupJob(user, profiles, sink, actions, timeExpression, keyRing);
		
		return job;
	}

	public List<BackupJob> getJobs(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteJob(String username, Long jobId) {
		// TODO Auto-generated method stub
		
	}

	public List<Status> getStatus(String username, Long jobId, Date fromDate,
			Date toDate) {
		// TODO Auto-generated method stub
		return null;
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
			String profileName, boolean source, String keyRing) throws PluginException,
			InvalidCredentialsException {
		Authorizable auth = plugins.getAuthorizable(uniqueDescIdentifier);
		AuthRequest ar = new AuthRequest();
		conn.begin();
		User user = getUserDao().findByName(username);
		if (user == null)
			throw new IllegalArgumentException(String.format("User %s doesn't exist!", username));
		Profile profile = new Profile(getUserDao().findByName(username), profileName, uniqueDescIdentifier, source);		
		switch (auth.getAuthType()) {
			case OAuth:
				OAuthBased oauth = plugins.getOAuthBasedAuthorizable(uniqueDescIdentifier);
				Properties p = new Properties();
				p.setProperty("callback", callbackUrl);
				String redirectUrl = oauth.createRedirectURL(p, callbackUrl);
				ar.setRedirectURL(redirectUrl);				
				for (Object key : p.keySet()) {
					String keyString = (String) key;
					profile.putEntry(keyString, p.getProperty(keyString));
				}				
		}
		profile = getProfileDao().save(profile);
		conn.commit();
		ar.setProfile(profile);
		return ar;
	}

	public void postAuth(long profileId, Properties props, String keyRing)
			throws PluginException, ValidationException,
			InvalidCredentialsException {
		conn.begin();
		Profile p = getProfileDao().findById(profileId);
		
		for (ProfileEntry pe : p.getEntries()) {
			props.setProperty(pe.getKey(), pe.getValue());
		}
		
		Authorizable auth = plugins.getAuthorizable(p.getDesc());
		if (auth.getAuthType() == AuthorizationType.InputBased) {
			InputBased inputBasedService = plugins.getInputBasedAuthorizable(p.getDesc());
			if (inputBasedService.isValid(props)) {
				auth.postAuthorize(props);
				for (Object key : props.keySet()) {
					String keyStr = (String) key;
					p.putEntry(keyStr, props.getProperty(keyStr));
				}
				getProfileDao().save(p);
				conn.commit();
				return;
			}
			else {
				conn.rollback();
				throw new ValidationException();
			}
		} else {
			auth.postAuthorize(props);
			for (Object key : props.keySet()) {
				String keyStr = (String) key;
				p.putEntry(keyStr, props.getProperty(keyStr));
			}
			//conn.begin();
			getProfileDao().save(p);			
			conn.commit();
		}
	}

	public long searchBackup(String username, String keyRingPassword,
			String query) {
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
		conn.setDataAccessLayer(dal);
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

	public EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}

	public void setEntityManagerFactory(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
		if (conn != null)
			conn.setEntityManagerFactory(emFactory);
	}
	
	public void setConnection(Connection conn) {
		this.conn = conn;
		if (emFactory != null)
			conn.setEntityManagerFactory(emFactory);
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	} 
}
