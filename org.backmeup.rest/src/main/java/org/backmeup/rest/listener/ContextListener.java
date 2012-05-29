package org.backmeup.rest.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.backmeup.logic.BusinessLogic;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class ContextListener implements ServletContextListener {

  private BusinessLogic logic;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("Starting business logic...");
    if (logic == null) {
      try {
        Weld weld = new Weld();
          WeldContainer container = weld.initialize();          
          logic = container.instance().select(BusinessLogic.class).get();
          sce.getServletContext().setAttribute("org.backmeup.logic", logic);
      } catch (Throwable e) {
        do {      
          e.printStackTrace();
          e = e.getCause();
        } while (e.getCause() != e && e.getCause() != null);
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // call shutdown on exit
    System.out.println("Shutting down business logic...");
    if (this.logic == null) {
      logic = (BusinessLogic) sce.getServletContext().getAttribute("org.backmeup.logic");
    }
    this.logic.shutdown();
    System.out.println("BL has been shut down!");
  }
}
