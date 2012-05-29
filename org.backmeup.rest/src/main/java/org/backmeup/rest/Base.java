package org.backmeup.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.backmeup.logic.BusinessLogic;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

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
	//@Context
	//private Providers providers;
	private BusinessLogic logic;
	
	@Context
	private ServletContext context;
	
	protected BusinessLogic getLogic() {
	  logic = (BusinessLogic) context.getAttribute("org.backmeup.logic");
	   
		if (logic == null) {
		  // just in case we are running in an embedded server
		  try {
          Weld weld = new Weld();
          WeldContainer container = weld.initialize();          
          logic = container.instance().select(BusinessLogic.class).get();
          context.setAttribute("org.backmeup.logic", logic);
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
