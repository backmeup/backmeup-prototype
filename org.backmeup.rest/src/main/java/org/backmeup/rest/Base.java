package org.backmeup.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.rest.cdi.JNDIBeanManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final Logger logger = LoggerFactory.getLogger(Base.class);
	
	private BusinessLogic logic;
	
	@Context
	private ServletContext context;
	
	protected BusinessLogic getLogic() {
	  logic = (BusinessLogic) context.getAttribute("org.backmeup.logic");
	   
		if (logic == null) {
		  // just in case we are running in an embedded server
		  try {
			  JNDIBeanManager jndiManager = JNDIBeanManager.getInstance();
			  logic = jndiManager.getBean(BusinessLogic.class);
			  context.setAttribute("org.backmeup.logic", logic);
      } catch (Throwable e) {
        do {      
        	logger.error("", e);
        	e = e.getCause();
        } while (e.getCause() != e && e.getCause() != null);
      }
		}
		
		return logic;
	}
}
