package org.backmeup.dal;

/**
 * The BaseDao interface provides basic CRUD (database) functionality
 * for a certain model class.
 * 
 * @author fschoeppl
 *
 * @param <T> The model class to use (@see org.backmeup.model)
 */
public interface BaseDao<T> {
	
	T findById(long id);
	
	T merge(T entity);
	
	boolean delete(T entity);
	
	T save(T entity);
	
	long count();
	
}
