package org.backmeup.dal.jpa;

import javax.persistence.EntityManager;

import org.backmeup.dal.SearchResponseDao;
import org.backmeup.model.SearchResponse;

public class SearchResponseDaoImpl extends BaseDaoImpl<SearchResponse> implements SearchResponseDao {

	public SearchResponseDaoImpl(EntityManager em) {
		super(em);
	}

}
