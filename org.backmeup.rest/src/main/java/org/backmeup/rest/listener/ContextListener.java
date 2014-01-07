package org.backmeup.rest.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.backmeup.configuration.Configuration;
import org.backmeup.job.impl.rabbitmq.RabbitMQJobReceiver;
import org.backmeup.logic.BusinessLogic;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener implements ServletContextListener {
	private final Logger logger = LoggerFactory.getLogger(ContextListener.class);
	
	//private BusinessLogic logic;
	//private Plugin plugins;
	//private Keyserver keyserver;
	//private BeanManager beanManager;

	public void InitializePluginInfrastructure() {
		
	}

	
	public void startKeyServer() {
		  //TODO: Exchange with https constructor; use parameters from bl.properties + truststores from plone
		/*
	    keyserver = new org.backmeup.keyserver.client.impl.Keyserver(
	      Configuration.getConfig().getProperty("keyserver.host"),
	      Configuration.getConfig().getProperty("keyserver.path"),
	      true
	    );
	    */
	    
	    //TODO: inject to RabbitMQJobReceiver

	}

	public List<RabbitMQJobReceiver> startRabbitMQWorker() throws IOException {
		int numberOfReceivers;
		try {
			numberOfReceivers = Integer.parseInt(Configuration.getConfig().getProperty("message.queue.receivers"));
		} catch (Exception e) {
			// Default to 4
			numberOfReceivers = 4;
		}

		// TODO that's just a quick hack
//		RabbitMQJobReceiver.initSystem(autodeploy.getAbsolutePath());

		List<RabbitMQJobReceiver> receivers = new ArrayList<RabbitMQJobReceiver>();
		for (int i = 0; i < numberOfReceivers; i++) {
			RabbitMQJobReceiver rec = new RabbitMQJobReceiver(Configuration
					.getConfig().getProperty("message.queue.host"),
					Configuration.getConfig().getProperty("message.queue.name"));
			//rec.setKeyserver(keyserver);
			rec.start();
			receivers.add(rec);
		}

		return receivers;
	}

	public Client startIndexClient() {
		String clusterName = "es-cluster-"+ NetworkUtils.getLocalAddress().getHostName();
		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
		settings.put("path.data", "C:\\Program Files (Dev)\\apache-tomcat-7.0.42\\data\\rest\\index");
		return NodeBuilder.nodeBuilder().settings(settings).clusterName(clusterName).node().client();
	}	

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			logger.info("Inititialize BackMeUp");

			//logger.info("Initialize plugin infrastructure");

			logger.info("Starting index client");
			//Client client = startIndexClient();
			//sce.getServletContext().setAttribute("org.backmeup.indexclient", client);

			//logger.info("Starting keyserver");
			//startKeyServer();

			logger.info("Starting job workers");
			List<RabbitMQJobReceiver> receivers = startRabbitMQWorker();
			logger.info(String.format("Started %d job workers!\n", receivers.size()));
			sce.getServletContext().setAttribute("org.backmeup.worker",	receivers);

		} catch (Throwable e) {
			do {
				logger.error("Error during startup of business logic / job workers",	e);
				e = e.getCause();
			} while (e.getCause() != e && e.getCause() != null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Shutting down business logic...");
		
		// call shutdown on exit
		BusinessLogic logic = (BusinessLogic) sce.getServletContext().getAttribute("org.backmeup.logic");
		if(logic != null) {
			logic.shutdown();
			logic = null;
		}

		List<RabbitMQJobReceiver> receivers = (List<RabbitMQJobReceiver>) sce
				.getServletContext().getAttribute("org.backmeup.worker");
		if (receivers != null) {
			logger.info("Shutting down job workers!");
			for (RabbitMQJobReceiver receiver : receivers) {
				receiver.stop();
			}
		}

		/*
		Client indexClient = (Client) sce.getServletContext().getAttribute(
				"org.backmeup.indexclient");
		if (indexClient != null) {
			logger.info("Shutting down index client");
			indexClient.close();
		}
		*/

		logger.info("Shutdown completed");
	}
}
