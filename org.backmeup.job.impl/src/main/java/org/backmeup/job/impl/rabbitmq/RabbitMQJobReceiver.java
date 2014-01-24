package org.backmeup.job.impl.rabbitmq;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.jpa.util.ConnectionImpl;
import org.backmeup.job.impl.BackupJobRunner;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.plugin.osgi.PluginImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * A Receiver class that listens to a RabbitMQ message queue and executes Backup
 * jobs that get sent across the wire.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class RabbitMQJobReceiver {
	/*
	 * private static final String EXPORTED_PACKAGES =
	 * "org.backmeup.plugin.spi " + "org.backmeup.model " +
	 * "org.backmeup.model.spi " + "org.backmeup.plugin.api.connectors " +
	 * "org.backmeup.plugin.api.storage " + "com.google.gson " +
	 * "org.backmeup.plugin.api " + "org.backmeup.plugin.api.actions " +
	 * "javax.mail " + "com.sun.imap ";
	 */
	private final Logger logger = LoggerFactory.getLogger(RabbitMQJobReceiver.class);
	
	private String indexHost;

	private Integer indexPort;

	private String jobTempDir;

	private String backupName;
	
	private String mqName;
	
	private String mqHost;
	
	
	
	private Plugin plugins;

	private Keyserver keyserver;

	private DataAccessLayer dal;

	

	private org.backmeup.dal.Connection conn;
	
	private EntityManagerFactory emFactory;

	private Connection mqConnection;

	private Channel mqChannel;

	private boolean listening;


	// TODO that's just a quick hack...
	// public static void initSystem(String pluginsDir) throws IOException {
	// // Start up the Plugin manager
	// //File osgiTemp =
	// File.createTempFile("C:\\Program Files (Dev)\\apache-tomcat-7.0.42\\data\\rest\\osgiTmp",
	// Long.toString(System.nanoTime()));
	// File osgiTemp = File.createTempFile("osgiTemp",
	// Long.toString(System.nanoTime()));
	// plugins = new PluginImpl(pluginsDir, osgiTemp.getAbsolutePath(),
	// EXPORTED_PACKAGES);
	// plugins.startup();
	// ((PluginImpl)plugins).waitForInitialStartup();
	// }

	public RabbitMQJobReceiver(String mqHost, String mqName, String indexHost, Integer indexPort, String backupName, String jobTempDir, Plugin plugins, Keyserver keyserver, DataAccessLayer dal) throws IOException {
		this.mqName = mqName;
		this.mqHost = mqHost;
		this.indexHost = indexHost;
		this.indexPort = indexPort;
		this.backupName = backupName;
		this.jobTempDir = jobTempDir;
		
		this.plugins = plugins;
		this.keyserver = keyserver;
		this.dal = dal;
		
		this.listening = false;

		// Connect to the message queue
		logger.info("Connecting to the message queue");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);

		mqConnection = factory.newConnection();
		mqChannel = mqConnection.createChannel();
		mqChannel.queueDeclare(mqName, false, false, false, null);

		// prepare data access layer
		// make sure you have a valid META-INF/persistence.xml file pointing to
		// the core database
		emFactory = Persistence.createEntityManagerFactory("org.backmeup.jpa");
		conn = new ConnectionImpl();
		((ConnectionImpl) conn).setDataAccessLayer(dal);
		((ConnectionImpl) conn).setEntityManagerFactory(emFactory);
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
						logger.info("Starting message queue receiver");
						QueueingConsumer consumer = new QueueingConsumer(
								mqChannel);
						mqChannel.basicConsume(mqName, true, consumer);

						while (listening) {
							try {
								QueueingConsumer.Delivery delivery = consumer
										.nextDelivery();
								String message = new String(delivery.getBody());
								logger.info("Received: " + message);

								BackupJob job = JsonSerializer.deserialize(
										message, BackupJob.class);

								Storage storage = new LocalFilesystemStorage();
								BackupJobRunner runner = new BackupJobRunner(
										plugins, keyserver, conn, dal, indexHost, indexPort, jobTempDir, backupName);
								runner.executeBackup(job, storage);
							} catch (Exception ex) {
								logger.error("failed to process job", ex);
							}
						}

						logger.info("Stopping message queue receiver");
						mqChannel.close();
						mqConnection.close();
						plugins.shutdown();
						emFactory.close();
						logger.info("Message queue receiver stopped");
					} catch (IOException e) {
						// Should only happen if message queue is down
						logger.error("message queue down", e);
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
