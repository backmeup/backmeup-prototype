package org.backmeup.logic.impl.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;

import org.backmeup.dal.DataAccessLayer;

@ApplicationScoped
public class Connection {
	private EntityManagerFactory emFactory;
	private EntityManager em;
	private DataAccessLayer dal;
	
	@Inject
	public void setEntityManagerFactory(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}
	
	@Inject
	public void setDataAccessLayer(DataAccessLayer dal) {
		this.dal = dal;
	}
	
	public void begin() {
		if (em == null) {
			em = emFactory.createEntityManager();
			dal.setConnection(em);
		}
		
		if (!em.getTransaction().isActive()) {		
			em.setFlushMode(FlushModeType.COMMIT);
			em.getTransaction().begin();		
		}
	}
	
	public void rollback() {
		if (em ==null)
			return;
		if (em.getTransaction().isActive())
			em.getTransaction().rollback();
	}
	
	public EntityManager getEntityManager() {
		return em;
	}
	
	public void commit() {
		if (em == null) {
			return;
		}
		if (em.getTransaction().isActive())
			em.getTransaction().commit();
	}
}
