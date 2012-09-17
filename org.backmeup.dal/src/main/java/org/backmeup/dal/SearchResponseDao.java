package org.backmeup.dal;

import org.backmeup.model.SearchResponse;

public interface SearchResponseDao extends BaseDao<SearchResponse> {
	
	public SearchResponse findById(long id);

}
