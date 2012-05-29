package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.ProfileDao;
import org.backmeup.model.Profile;

/**
 * The ProfileDaoImpl realizes the ProfileDao interface with 
 * JPA specific operations.
 * 
 * 
 * @author fschoeppl
 *
 */
public class ProfileDaoImpl extends BaseDaoImpl<Profile> implements ProfileDao {

	public ProfileDaoImpl(EntityManager em) {
		super(em);
	}

	@SuppressWarnings("unchecked")
	public List<Profile> findDatasourceProfilesByUsername(String username) {		
		Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.username = :username AND p.source = :source");
		q.setParameter("username", username);				
		q.setParameter("source", true);
		List<Profile> profiles = q.getResultList();		
		return profiles;
	}

  @SuppressWarnings("unchecked")
  @Override
  public List<Profile> findProfilesByUsernameAndService(String username,
      String sourceSinkId) {
    Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.username = :username AND p.desc = :id ");
    q.setParameter("username", username);        
    q.setParameter("id", sourceSinkId);
    List<Profile> profiles = q.getResultList(); 
    return profiles;
  }

}
