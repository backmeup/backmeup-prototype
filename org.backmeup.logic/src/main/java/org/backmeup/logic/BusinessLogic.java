package org.backmeup.logic;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.User;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;

/**
 * The BusinessLogic interface contains 
 * all available operations of this project.
 * 
 * It should delegate its operations to other
 * layers so that these can be exchanged more easily.
 * 
 * The org.backmeup.rest project uses this interface
 * to realize its operations.
 * 
 * @author fschoeppl
 *
 */
public interface BusinessLogic {
	
	// user operations 
	public User getUser(String username);
	public User deleteUser(String username);
	public User changeUser(String username, String oldPassword, String newPassword, String newKeyRing, String newEmail);
	public User login(String username, String password);
	public User register(String username, String password, String keyRing, String email) throws AlreadyRegisteredException, IllegalArgumentException;
	
	//datasource operations
	public List<SourceSinkDescribable> getDatasources();
	public List<Profile> getDatasourceProfiles(String username);
	public Profile deleteProfile(String username, Long profile);
	public List<String> getDatasourceOptions(String username, Long profileId, String keyRingPassword);
	public void changeProfile(Long profileId, List<String> sourceOptions);
	public void uploadDatasourcePlugin(String filename, InputStream data);
	public void deleteDatasourcePlugin(String name);
	
	//datasink operations
	public List<SourceSinkDescribable> getDatasinks();
	public List<Profile> getDatasinkProfiles(String username);
	public void uploadDatasinkPlugin(String filename, InputStream data);
	public void deleteDatasinkPlugin(String name);
	
	// profile operation
	public void addProfileEntries(Long profileId, Properties entries);
	
	//validate profile operation
	public ValidationNotes validateProfile(String username, Long profileId);
	
	//metadata operations	
	public Properties getMetadata(String username, Long profileId);
	
	//action operations
	public List<ActionDescribable> getActions();
	public List<String> getActionOptions(String actionId);
	public void uploadActionPlugin(String filename, InputStream data);
	public void deleteActionPlugin(String name);
	
	//job & validation operations
	public ValidationNotes validateBackupJob(String username, Long jobId);	
	public BackupJob createBackupJob(String username, List<Long> sourceProfiles, Long sinkProfileId, Map<Long, String[]> sourceOptions, String[] requiredActions, String timeExpression, String keyRing);
	public List<BackupJob> getJobs(String username);
	public void deleteJob(String username, Long jobId);
	public List<Status> getStatus(String username, Long jobId, Date fromDate, Date toDate);
	public ProtocolDetails getProtocolDetails(String username, Long fileId);
	public ProtocolOverview getProtocolOverview(String username, String duration);
	
	//datasink/-source auth operations
	public AuthRequest preAuth(String username, String sourceSinkId, String profileName, String keyRing) throws PluginException, InvalidCredentialsException;
	public void postAuth(Long profileId, Properties props, String keyRing) throws PluginException, ValidationException, InvalidCredentialsException;
	
	//search operations
	public long searchBackup(String username, String keyRingPassword, String query);
	public SearchResponse queryBackup(String username, long searchId, String filterType, String filterValue);
	
	//misc operations
	public void shutdown();
}
