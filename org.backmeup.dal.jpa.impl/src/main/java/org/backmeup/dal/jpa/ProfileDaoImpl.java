package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.ProfileDao;
import org.backmeup.model.Profile;

public class ProfileDaoImpl extends BaseDaoImpl<Profile> implements ProfileDao {

	public ProfileDaoImpl(EntityManager em) {
		super(em);
	}

	@SuppressWarnings("unchecked")
	public List<Profile> findDatasourceProfilesByUsername(String username) {		
		Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE username = ? AND source = ?");
		q.setParameter(1, username);				
		q.setParameter(2, true);
		List<Profile> profiles = q.getResultList();		
		return profiles;
	}

}
