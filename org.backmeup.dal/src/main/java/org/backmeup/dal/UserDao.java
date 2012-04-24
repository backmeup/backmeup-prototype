package org.backmeup.dal;

import org.backmeup.model.User;

public interface UserDao extends BaseDao<User> {

	public User findByName(String username);
	
}
