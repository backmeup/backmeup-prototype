package org.backmeup.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.backmeup.logic.BusinessLogic;

/**
 * All rest classes derive from this class to 
 * gain access to the BusinessLogic.
 * 
 * Note: The derived classes always delegate the incoming REST call
 *       to the business logic.
 * 
 * @author fschoeppl
 *
 */
public class Base {
	@Context
	private Providers providers;
	private BusinessLogic logic;
	
	protected BusinessLogic getLogic() {
		if (logic == null) {
			ContextResolver<BusinessLogic> logicResolver = providers.getContextResolver(BusinessLogic.class, null);
			logic = logicResolver.getContext(BusinessLogic.class);
		}
		return logic;
	}
}
