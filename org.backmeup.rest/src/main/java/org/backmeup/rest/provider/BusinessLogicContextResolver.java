package org.backmeup.rest.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.backmeup.logic.BusinessLogic;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * This class looks up the BusinessLogic implementation
 * by using Weld.
 * 
 * @author fschoeppl
 *
 */
@Provider
public class BusinessLogicContextResolver implements ContextResolver<BusinessLogic> {
	private BusinessLogic logic;
	
	public BusinessLogic getContext(Class<?> arg0) {	
		if (logic == null) {
			try {
				Weld weld = new Weld();
			    WeldContainer container = weld.initialize();			    
			    logic = container.instance().select(BusinessLogic.class).get();
			} catch (Throwable e) {
				do {			
					e.printStackTrace();
					e = e.getCause();
				} while (e.getCause() != e && e.getCause() != null);
			}
		}
		return logic;
	}

}
