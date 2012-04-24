package org.backmeup.dal;


public interface DataAccessLayer {
	
	public UserDao createUserDao();
	
	public ProfileDao createProfileDao();
	
	public void setConnection(Object connection);
	
}
