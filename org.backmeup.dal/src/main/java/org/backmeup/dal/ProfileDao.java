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

	List<Profile> findDatasourceProfilesByUsername(String username);

}
