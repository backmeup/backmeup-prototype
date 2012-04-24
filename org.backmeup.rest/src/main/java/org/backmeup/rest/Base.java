package org.backmeup.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.backmeup.logic.BusinessLogic;

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
