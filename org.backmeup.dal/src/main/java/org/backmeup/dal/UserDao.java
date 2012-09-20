package org.backmeup.dal;

import org.backmeup.model.BackMeUpUser;

/**
  * The UserDao contains all database relevant
 * operations for the model class User.
 * 
 * @author fschoeppl
 *
 */
public interface UserDao extends BaseDao<BackMeUpUser> {

	public BackMeUpUser findByName(String username);
	
	public BackMeUpUser findByVerificationKey(String verificationKey);
	
	public BackMeUpUser findByEmail(String email);
	
}
