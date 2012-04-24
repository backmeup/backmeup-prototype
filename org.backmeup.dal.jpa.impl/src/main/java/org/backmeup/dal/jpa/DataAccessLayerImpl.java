package org.backmeup.dal.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.UserDao;

@ApplicationScoped
public class DataAccessLayerImpl implements DataAccessLayer {
	private EntityManager em;
	
	public DataAccessLayerImpl() {	
	}

	public UserDao createUserDao() { 
		return new UserDaoImpl(em);
	}

	public ProfileDao createProfileDao() { 
		return new ProfileDaoImpl(em);
	}

	public void setConnection(Object connection) {
		this.em = (EntityManager) connection;
	}
	

}
