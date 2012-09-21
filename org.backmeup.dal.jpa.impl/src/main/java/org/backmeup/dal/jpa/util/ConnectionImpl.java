package org.backmeup.dal.jpa.util;

import java.util.Stack;

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
	private ThreadLocal<Stack<Boolean>> joinedTransactions;
	private DataAccessLayer dal;
		
	public ConnectionImpl() {
	  this.threadLocalEntityManager = new ThreadLocal<EntityManager>();
	  joinedTransactions = new ThreadLocal<Stack<Boolean>>(); 
	}
	
	@Inject
	public void setEntityManagerFactory(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}
	
	@Inject
	public void setDataAccessLayer(DataAccessLayer dal) {
		this.dal = dal;
	}
	
	private EntityManager getOrCreateEntityManager() {
	  EntityManager em = getEntityManager();
    
    if (em == null) {
      em = emFactory.createEntityManager();
      threadLocalEntityManager.set(em);
      dal.setConnection(em);
    }
    
    return em;
	}
	
	public void begin() {	  
	  EntityManager em = getOrCreateEntityManager(); 
		
		if (em.getTransaction().isActive()) {
		  System.out.println("Warning: Transaction already active! Rolling back");
		  em.getTransaction().rollback();
		}
		
		if (!em.getTransaction().isActive()) {		
			em.setFlushMode(FlushModeType.COMMIT);
			em.getTransaction().begin();		
		}
	}
	
	public void rollback() {
	  EntityManager em = getEntityManager();
	  
		if (em == null) {		  
		  return;
		}
		
		Stack<Boolean> transactionStack = joinedTransactions.get();
		if (transactionStack == null || transactionStack.isEmpty()) {
  		if (em.getTransaction().isActive()) {
  			em.getTransaction().rollback();
  		}
  		resetEntityManager();
		} else {
		  transactionStack.pop();
		}
	}
	
	public EntityManager getEntityManager() {
		return threadLocalEntityManager.get();
	}
	
	public void commit() {
	  EntityManager em = getEntityManager();
	  
		if (em == null) {
		  System.err.println("Has already been committed/rolled back!");
			return;
		}
		if (em.getTransaction().isActive()) {
			em.getTransaction().commit();
		}
		resetEntityManager();
	}
	
	private void resetEntityManager() {
	  EntityManager em = getEntityManager();
	  if (em != null)
	    em.close();
	  threadLocalEntityManager.set(null);
	  dal.setConnection(null);
	}

  @Override
  public void beginOrJoin() {
    EntityManager em = getOrCreateEntityManager();
    
    if (!em.getTransaction().isActive()) {    
      em.setFlushMode(FlushModeType.COMMIT);
      em.getTransaction().begin();
    } else {
      Stack<Boolean> transactionStack = joinedTransactions.get();
      if (transactionStack == null) 
        transactionStack = new Stack<Boolean>();
      transactionStack.push(true);
      joinedTransactions.set(transactionStack);
    }
  }
}
