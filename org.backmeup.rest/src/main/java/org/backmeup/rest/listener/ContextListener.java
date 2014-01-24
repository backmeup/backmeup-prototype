package org.backmeup.rest.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.backmeup.logic.BusinessLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener implements ServletContextListener {
	private final Logger logger = LoggerFactory.getLogger(ContextListener.class);
	
	/*
	public List<RabbitMQJobReceiver> startRabbitMQWorker() throws IOException {
		int numberOfReceivers;
		try {
			numberOfReceivers = Integer.parseInt(Configuration.getConfig().getProperty("message.queue.receivers"));
		} catch (Exception e) {
			// Default to 4
			numberOfReceivers = 4;
		}

		List<RabbitMQJobReceiver> receivers = new ArrayList<RabbitMQJobReceiver>();
		for (int i = 0; i < numberOfReceivers; i++) {
			RabbitMQJobReceiver rec = new RabbitMQJobReceiver(Configuration
					.getConfig().getProperty("message.queue.host"),
					Configuration.getConfig().getProperty("message.queue.name"));
			rec.start();
			receivers.add(rec);
		}

		return receivers;
	}
	*/

	@Override
	public void contextInitialized(ServletContextEvent sce) {
//		try {
//			logger.info("Inititialize BackMeUp");
//
//			logger.info("Starting job workers");
//			List<RabbitMQJobReceiver> receivers = startRabbitMQWorker();
//			logger.info(String.format("Started %d job workers!\n", receivers.size()));
//			sce.getServletContext().setAttribute("org.backmeup.worker",	receivers);
//
//		} catch (Throwable e) {
//			do {
//				logger.error("Error during startup of business logic / job workers",	e);
//				e = e.getCause();
//			} while (e.getCause() != e && e.getCause() != null);
//		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Shutting down business logic...");
		
		// call shutdown on exit
		BusinessLogic logic = (BusinessLogic) sce.getServletContext().getAttribute("org.backmeup.logic");
		if(logic != null) {
			logic.shutdown();
			logic = null;
		}

//		List<RabbitMQJobReceiver> receivers = (List<RabbitMQJobReceiver>) sce
//				.getServletContext().getAttribute("org.backmeup.worker");
//		if (receivers != null) {
//			logger.info("Shutting down job workers!");
//			for (RabbitMQJobReceiver receiver : receivers) {
//				receiver.stop();
//			}
//		}

		logger.info("Shutdown completed");
	}
}
