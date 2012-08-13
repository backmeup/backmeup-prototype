package org.backmeup.dal.jpa;

import javax.persistence.EntityManager;

import org.backmeup.dal.ServiceDao;
import org.backmeup.model.Service;

public class ServiceDaoImpl extends BaseDaoImpl<Service> implements ServiceDao {

	public ServiceDaoImpl(EntityManager em) {
		super(em);
	}

}
