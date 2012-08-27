package org.backmeup.job.impl.rabbitmq;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.backmeup.job.impl.BackupJobRunner;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;
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
	
	private Keyserver keyserver;
	
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
		log.info("Starting plugin framework");
		this.plugins = new PluginImpl(pluginsDir, osgiTempDir, EXPORTED_PACKAGES);
		this.plugins.startup();
	    ((PluginImpl)plugins).waitForInitialStartup();
	    
	  //TODO: Exchange with https constructor; use parameters from bl.properties + truststores from plone
	    keyserver = new org.backmeup.keyserver.client.impl.Keyserver(       
	      "keysrv.backmeup.at",
	      "/keysrv",
	      true
	    );
	    
	    // Connect to the message queue
	    log.info("Connecting to the message queue");
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
						log.info("Starting message queue receiver");
					    QueueingConsumer consumer = new QueueingConsumer(mqChannel);
					    mqChannel.basicConsume(mqName, true, consumer);
						
					    while (listening) {
					    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					    	String message = new String(delivery.getBody());
					    	log.info("Received: " + message);
					    	
					    	BackupJob job = JsonSerializer.deserialize(message, BackupJob.class);
					    	
			                StorageReader reader = new LocalFilesystemStorageReader();
			                StorageWriter writer = new LocalFilesystemStorageWriter();
			                
			        		BackupJobRunner runner = new BackupJobRunner(plugins, keyserver);
			        		runner.executeBackup(job, reader, writer);
					    }
					    
					    log.info("Stopping message queue receiver");
					    mqChannel.close();
					    mqConnection.close();
					    plugins.shutdown();
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
