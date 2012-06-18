package org.backmeup.logic.impl.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;

import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
/**
 * The Connection class makes the JPA transaction handling
 * easier for the BusinessLogicImpl class.
 * 
 * @author fschoeppl
 *
 */
@ApplicationScoped
public class ConnectionImpl implements Connection {
	private EntityManagerFactory emFactory;
	private ThreadLocal<EntityManager> threadLocalEntityManager;	
	private DataAccessLayer dal;
		
	public ConnectionImpl() {
	  this.threadLocalEntityManager = new ThreadLocal<EntityManager>();
	}
	
	@Inject
	public void setEntityManagerFactory(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}
	
	@Inject
	public void setDataAccessLayer(DataAccessLayer dal) {
		this.dal = dal;
	}
	
	public void begin() {	  
	  EntityManager em = getEntityManager();
	  
		if (em == null) {
		  em = emFactory.createEntityManager();
		  threadLocalEntityManager.set(em);
			dal.setConnection(em);
		}
		
		if (!em.getTransaction().isActive()) {		
			em.setFlushMode(FlushModeType.COMMIT);
			em.getTransaction().begin();		
		}
	}
	
	public void rollback() {
	  EntityManager em = getEntityManager();
	  
		if (em == null)
			return;
		
		if (em.getTransaction().isActive()) {
			em.getTransaction().rollback();
		}
	}
	
	public EntityManager getEntityManager() {
		return threadLocalEntityManager.get();
	}
	
	public void commit() {
	  EntityManager em = getEntityManager();
	  
		if (em == null) {
			return;
		}
		if (em.getTransaction().isActive()) {
			em.getTransaction().commit();
		}
	}
	
	private void resetEntityManager() {
	  threadLocalEntityManager.set(null);
	  dal.setConnection(null);
	}

  @Override
  public void releaseConnection() {
    resetEntityManager();
  }
}
