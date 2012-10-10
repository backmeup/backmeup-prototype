package org.backmeup.dal;




/**
 * The DataAccessLayer provides access to any kind of 
 * database. It uses Data Access Objects (e.g. UserDao) 
 * to store, retrieve and delete data of a certain
 * database.
 * 
 * @author fschoeppl
 *
 */
public interface DataAccessLayer {
	
	public UserDao createUserDao();
	
	public ProfileDao createProfileDao();
	
	public StatusDao createStatusDao();
	
	public BackupJobDao createBackupJobDao();
	
	public ServiceDao createServiceDao();
	
	public JobProtocolDao createJobProtocolDao();
	
	public SearchResponseDao createSearchResponseDao();
	
	public void setConnection(Object connection);
	
}
