package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.UserDao;
import org.backmeup.model.BackMeUpUser;

/**
 * The ProfileDaoImpl realizes the ProfileDao interface with 
 * JPA specific operations.
 * 
 * @author fschoeppl
 *
 */
public class UserDaoImpl extends BaseDaoImpl<BackMeUpUser> implements UserDao {

	public UserDaoImpl(EntityManager em) {
		super(em);
	}

	@SuppressWarnings("unchecked")
	public BackMeUpUser findByName(String username) {
		Query q = em.createQuery("SELECT u FROM BackMeUpUser u WHERE username = :username");
		q.setParameter("username", username);		
		List<BackMeUpUser> users = q.getResultList();
		BackMeUpUser u = users.size() > 0 ? users.get(0) : null;		
		return u;
	}

	@SuppressWarnings("unchecked")
  @Override
  public BackMeUpUser findByVerificationKey(String verificationKey) {
    Query q = em.createQuery("SELECT u FROM BackMeUpUser u WHERE verificationKey = :verificationKey");
    q.setParameter("verificationKey", verificationKey);    
    List<BackMeUpUser> users = q.getResultList();
    BackMeUpUser u = users.size() > 0 ? users.get(0) : null;    
    return u;
  }

	@SuppressWarnings("unchecked")
  @Override
  public BackMeUpUser findByEmail(String email) {
    Query q = em.createQuery("SELECT u FROM BackMeUpUser u WHERE email = :email");
    q.setParameter("email", email);
    List<BackMeUpUser> users = q.getResultList();
    BackMeUpUser u = users.size() > 0 ? users.get(0) : null;    
    return u;
  }
}
