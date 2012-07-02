package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.Profile;

/**
 * The ProfileDao contains all database relevant
 * operations for the model class Profile.
 * 
 * @author fschoeppl
 *
 */
public interface ProfileDao extends BaseDao<Profile> {

  List<Profile> findProfilesByUsername(String username);
  
	List<Profile> findDatasourceProfilesByUsername(String username);
	
	List<Profile> findDatasinkProfilesByUsername(String username);

	List<Profile> findProfilesByUsernameAndService(String username, String sourceSinkId);
}
