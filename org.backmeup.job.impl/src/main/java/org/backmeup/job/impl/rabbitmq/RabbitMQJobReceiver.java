package org.backmeup.job.impl.rabbitmq;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.osgi.PluginImpl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * A Receiver class that listens to a RabbitMQ message queue and executes Backup jobs that
 * get sent across the wire.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class RabbitMQJobReceiver {
	
	private static final String EXPORTED_PACKAGES =
			"org.backmeup.plugin.spi " +
			"org.backmeup.model " +
			"org.backmeup.model.spi " +
			"org.backmeup.plugin.api.connectors " +
			"org.backmeup.plugin.api.storage " +
			"com.google.gson " + 
			"org.backmeup.plugin.api";
	
	private Plugin plugins;
	
	/**
	 * Message queue name
	 */
	private String mqName;
	
	/**
	 * Message queue connection
	 */
	private Connection mqConnection;
	
	/**
	 * Message queue channel
	 */
	private Channel mqChannel;
	
	/**
	 * Flag to switch message listening on and off
	 */
	private boolean listening = false;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public RabbitMQJobReceiver(String mqHost, String mqName, String pluginsDir, String osgiTempDir) throws IOException {
		this.mqName = mqName;
		
		// Start up the Plugin manager
		this.plugins = new PluginImpl(pluginsDir, osgiTempDir, EXPORTED_PACKAGES);
		this.plugins.startup();
	    ((PluginImpl)plugins).waitForInitialStartup();
	    
	    // Connect to the message queue
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(mqHost);
	    
	    mqConnection = factory.newConnection();
	    mqChannel = mqConnection.createChannel();
	    mqChannel.queueDeclare(mqName, false, false, false, null);
	}
	
	public boolean isListening() {
		return listening;
	}
	
	public void start() {
		if (!listening) {
			listening = true;
			
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
					    QueueingConsumer consumer = new QueueingConsumer(mqChannel);
					    mqChannel.basicConsume(mqName, true, consumer);
						
					    while (listening) {
					    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					    	String message = new String(delivery.getBody());
					    	log.info("Received: " + message);
					    }
					    
					    log.info("Stopping message queue receiver");
					    mqChannel.close();
					    mqConnection.close();
					    log.info("Message queue receiver stopped");
					} catch (IOException e) {
						// Should only happen if message queue is down
						log.fatal(e.getMessage() + " - message queue down?");
						throw new RuntimeException(e);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);						
					}
				}
			});
			
			t.start();
		}
	}
	
	public void stop() {
		listening = false;
	}

}
