package org.backmeup.rest.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.backmeup.configuration.Configuration;
import org.backmeup.job.impl.rabbitmq.RabbitMQJobReceiver;
import org.backmeup.logic.BusinessLogic;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.NodeBuilder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class ContextListener implements ServletContextListener {

  private Logger logger = Logger.getLogger(ContextListener.class.getName());
  private BusinessLogic logic;
  
  
  public static List<RabbitMQJobReceiver> startRabbitMQWorker() throws IOException {
    //File autodeploy = new File("autodeploy");
    File autodeploy = new File("C:\\Program Files (Dev)\\apache-tomcat-7.0.42\\data\\rest\\autodeploy");
    
    int numberOfReceivers;
    try {
      numberOfReceivers = Integer.parseInt(Configuration.getConfig().getProperty("message.queue.receivers"));
    } catch (Exception e) {
      // Default to 4
      numberOfReceivers = 4;
    }
    
    // TODO that's just a quick hack
    RabbitMQJobReceiver.initSystem(autodeploy.getAbsolutePath());
    
    List<RabbitMQJobReceiver> receivers = new ArrayList<RabbitMQJobReceiver>();
    for (int i=0; i<numberOfReceivers; i++) {
      RabbitMQJobReceiver rec = new RabbitMQJobReceiver(Configuration.getConfig().getProperty("message.queue.host"), Configuration.getConfig().getProperty("message.queue.name"));
      rec.start();
      receivers.add(rec);
    }   
    
    return receivers;
  }
  
  public static Client startIndexClient() {
	  String clusterName = "es-cluster-" + NetworkUtils.getLocalAddress().getHostName();
	  ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
	  settings.put("path.data", "C:\\Program Files (Dev)\\apache-tomcat-7.0.42\\data\\rest\\index");
    return NodeBuilder.nodeBuilder().settings(settings).clusterName(clusterName).node().client();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {    
    if (logic == null) {
      try {
          logger.info("Starting business logic...");
          Weld weld = new Weld();
          WeldContainer container = weld.initialize();
          logger.info("Starting index client...");
          Client client = startIndexClient();
          sce.getServletContext().setAttribute("org.backmeup.indexclient", client);
          logger.info("ElasticSearch index running at http://localhost:9200/");
          logger.info("Starting job workers ...");
          List<RabbitMQJobReceiver> receivers = startRabbitMQWorker();
          logger.info(String.format("Started %d job workers!\n", receivers.size()));
          sce.getServletContext().setAttribute("org.backmeup.worker", receivers);
          logic = container.instance().select(BusinessLogic.class).get();
          sce.getServletContext().setAttribute("org.backmeup.logic", logic);                    
      } catch (Throwable e) {
        do {      
          logger.log(Level.SEVERE, "Error during startup of business logic / job workers", e);          
          e = e.getCause();
        } while (e.getCause() != e && e.getCause() != null);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // call shutdown on exit
    if (this.logic == null) {      
      logic = (BusinessLogic) sce.getServletContext().getAttribute("org.backmeup.logic");     
    }
    logger.info("Shutting down business logic...");
    logic.shutdown();    
    this.logic = null;
    
    List<RabbitMQJobReceiver> receivers = (List<RabbitMQJobReceiver>) sce.getServletContext().getAttribute("org.backmeup.worker");
    if (receivers != null) {
      logger.info("Shutting down job workers!");
      for (RabbitMQJobReceiver receiver : receivers) {
        receiver.stop();
      }
    }
    
    Client indexClient = (Client) sce.getServletContext().getAttribute("org.backmeup.indexclient");
    if (indexClient != null) {
      logger.info("Shutting down index client");
      indexClient.close();      
    }
    
    logger.info("Shutdown completed");
  }
}
