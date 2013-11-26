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
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.osgi.PluginImpl;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.NodeBuilder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class ContextListener implements ServletContextListener {

	private static final String EXPORTED_PACKAGES = ""
			+ "org.backmeup.plugin.spi "
			+ "org.backmeup.model " 
			+ "org.backmeup.model.spi "
			+ "org.backmeup.plugin.api.connectors "
			+ "org.backmeup.plugin.api.storage " 
			+ "com.google.gson "
			+ "org.backmeup.plugin.api " 
			+ "org.backmeup.plugin.api.actions "
			+ "javax.mail " 
			+ "com.sun.imap ";

	private Logger logger = Logger.getLogger(ContextListener.class.getName());
	private BusinessLogic logic;
	private Plugin plugins;
	private Keyserver keyserver;

	public void InitializePluginInfrastructure() {
		// Start up the plug in manager
//		try {
//			File pluginsDir = new File("C:\\Program Files (Dev)\\apache-tomcat-7.0.42\\data\\rest\\autodeploy");
//			File osgiTemp = File.createTempFile("osgiTemp", Long.toString(System.nanoTime()));
//			plugins = new PluginImpl(pluginsDir.getAbsolutePath(), osgiTemp.getAbsolutePath(), EXPORTED_PACKAGES);
//			plugins.startup();
//			((PluginImpl) plugins).waitForInitialStartup();
//		} catch (IOException e) {
//			logger.log(Level.SEVERE, e.getStackTrace().toString());
//		}
		
		
		
		//TODO: inject evvverywherrrreeeeee;
	}

	public void startKeyServer() {
		  //TODO: Exchange with https constructor; use parameters from bl.properties + truststores from plone
	    keyserver = new org.backmeup.keyserver.client.impl.Keyserver(
	      Configuration.getConfig().getProperty("keyserver.host"),
	      Configuration.getConfig().getProperty("keyserver.path"),
	      true
	    );
	    
	    //TODO: inject to RabbitMQJobReceiver

	}

	public List<RabbitMQJobReceiver> startRabbitMQWorker() throws IOException {
		int numberOfReceivers;
		try {
			numberOfReceivers = Integer.parseInt(Configuration.getConfig()
					.getProperty("message.queue.receivers"));
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
		if (logic == null) {
			try {
				logger.info("Inititialize BackMeUp");
				Weld weld = new Weld();
				WeldContainer container = weld.initialize();
				
				logger.info("Initialize plugin infrastructure");
//				InitializePluginInfrastructure();
				plugins = container.instance().select(Plugin.class).get();
				
				logger.info("Starting index client");
				Client client = startIndexClient();
				sce.getServletContext().setAttribute("org.backmeup.indexclient", client);
//				logger.info("ElasticSearch index running at http://localhost:9200/");
				
				logger.info("Starting keyserver");
				startKeyServer();
				
				logger.info("Starting job workers");
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
			logic = (BusinessLogic) sce.getServletContext().getAttribute(
					"org.backmeup.logic");
		}
		logger.info("Shutting down business logic...");
		logic.shutdown();
		this.logic = null;

		List<RabbitMQJobReceiver> receivers = (List<RabbitMQJobReceiver>) sce
				.getServletContext().getAttribute("org.backmeup.worker");
		if (receivers != null) {
			logger.info("Shutting down job workers!");
			for (RabbitMQJobReceiver receiver : receivers) {
				receiver.stop();
			}
		}

		Client indexClient = (Client) sce.getServletContext().getAttribute(
				"org.backmeup.indexclient");
		if (indexClient != null) {
			logger.info("Shutting down index client");
			indexClient.close();
		}

		logger.info("Shutdown completed");
	}
}
