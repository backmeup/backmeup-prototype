package org.backmeup.dal.jpa;

import java.lang.reflect.ParameterizedType;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.BaseDao;
 
/**
 * Realizes the CRUD operations for a model class <T>
 * based on the JPA (EntityManager).
 * 
 * @author fschoeppl
 *
 * @param <T> The model class to use
 */
public abstract class BaseDaoImpl<T> implements BaseDao<T> {
	protected EntityManager em;
	protected Class<T> entityClass;	
	
	@SuppressWarnings("unchecked")
	public BaseDaoImpl(EntityManager em) {
		this.em = em;
		ParameterizedType superType = (ParameterizedType) this.getClass().getGenericSuperclass();
		entityClass = (Class<T>) superType.getActualTypeArguments()[0];
	} 
	
	public T merge(T entity) {
	  return em.merge(entity);
	}
 
	public T findById(long id) {
		T item = em.find(entityClass, id);
		return item;
	}
 
	public boolean delete(T entity) {
		entity = em.merge(entity);
		em.remove(entity);
		return true;
	}
 
	public T save(T entity) {
		entity = em.merge(entity);		
		return entity;
	} 
	
	public long count() {
		Query q = em.createQuery("SELECT COUNT(u) FROM " + entityClass.getName() + " u");
		Long cnt = (Long) q.getSingleResult();
		return cnt;
	} 
}
