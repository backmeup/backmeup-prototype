package org.backmeup.job.impl.rabbitmq;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.backmeup.configuration.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.jpa.DataAccessLayerImpl;
import org.backmeup.dal.jpa.util.ConnectionImpl;
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
			"org.backmeup.plugin.api " + 
			"org.backmeup.plugin.api.actions " +
			"javax.mail " +
			"com.sun.imap ";
	
	private Plugin plugins;
	
	private Keyserver keyserver;
	
	private DataAccessLayer dal;
	private org.backmeup.dal.Connection conn;
	
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

  private EntityManagerFactory emFactory;
	
	public RabbitMQJobReceiver(String mqHost, String mqName, String pluginsDir) throws IOException {
		this.mqName = mqName;
		
		// Start up the Plugin manager
		log.info("Starting plugin framework");
		File osgiTemp = File.createTempFile("osgi-temp", Long.toString(System.nanoTime()));
		this.plugins = new PluginImpl(pluginsDir, osgiTemp.getAbsolutePath(), EXPORTED_PACKAGES);
		this.plugins.startup();
	    ((PluginImpl)plugins).waitForInitialStartup();
	    
	  //TODO: Exchange with https constructor; use parameters from bl.properties + truststores from plone
	    keyserver = new org.backmeup.keyserver.client.impl.Keyserver(
	      Configuration.getConfig().getProperty("keyserver.host"),
	      Configuration.getConfig().getProperty("keyserver.path"),
	      true
	    );
	    
	    // Connect to the message queue
	    log.info("Connecting to the message queue");
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(mqHost);
	    
	    mqConnection = factory.newConnection();
	    mqChannel = mqConnection.createChannel();
	    mqChannel.queueDeclare(mqName, false, false, false, null);
	  
	  // prepare data access layer
	  // TODO: Use weld to setup the RabbitMQJobReceiver instead of manual weaving 
	  dal = new DataAccessLayerImpl();
	  // make sure you have a valid META-INF/persistence.xml file pointing to the core database 
	  emFactory = Persistence.createEntityManagerFactory("org.backmeup.jpa");
	  conn = new ConnectionImpl();
	  ((ConnectionImpl)conn).setDataAccessLayer(dal);
	  ((ConnectionImpl)conn).setEntityManagerFactory(emFactory);	  
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
					      try {
  					    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
  					    	String message = new String(delivery.getBody());
  					    	log.info("Received: " + message);
  					    	
  					    	BackupJob job = JsonSerializer.deserialize(message, BackupJob.class);
  					    	
  			                StorageReader reader = new LocalFilesystemStorageReader();
  			                StorageWriter writer = new LocalFilesystemStorageWriter();
  			                
  			        		BackupJobRunner runner = new BackupJobRunner(plugins, keyserver, conn, dal);
  			        		runner.executeBackup(job, reader, writer);
					      } catch (Exception ex) {
					        //TODO Log exception
					        ex.printStackTrace();
					        log.fatal(ex.getMessage() + " - failed to process job");
					      }
					    }
					    
					    log.info("Stopping message queue receiver");
					    mqChannel.close();
					    mqConnection.close();
					    plugins.shutdown();
					    emFactory.close();
					    log.info("Message queue receiver stopped");
					} catch (IOException e) {
						// Should only happen if message queue is down
						log.fatal(e.getMessage() + " - message queue down?");
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
