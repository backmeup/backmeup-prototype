package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.UserDao;
import org.backmeup.model.User;

public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {

	public UserDaoImpl(EntityManager em) {
		super(em);
	}

	@SuppressWarnings("unchecked")
	public User findByName(String username) {
		Query q = em.createQuery("SELECT u FROM User u WHERE username = ?");
		q.setParameter(1, username);		
		List<User> users = q.getResultList();
		User u = users.size() > 0 ? users.get(0) : null;		
		return u;
	}

}
