package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.Profile;

public interface ProfileDao extends BaseDao<Profile> {

	List<Profile> findDatasourceProfilesByUsername(String username);

}
