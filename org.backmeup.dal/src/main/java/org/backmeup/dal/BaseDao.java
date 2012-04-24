package org.backmeup.dal;

public interface BaseDao<T> {
	
	T findById(long id);
	
	boolean delete(T entity);
	
	T save(T entity);
	
	long count();
	
}
