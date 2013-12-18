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

	@Inject
	@Named("plugin")
	private Plugin plugins;

	@Inject
	private Keyserver keyserver;

	@Inject
	private DataAccessLayer dal;

	@Inject
	@Configuration(key = "backmeup.index.host", mandatory = true)
	private String indexHost;

	@Inject
	@Configuration(key = "backmeup.index.port")
	private Integer indexPort;

	@Inject
	@Configuration(key = "backmeup.job.temporaryDirectory", defaultValue = "/data/tmp_files")
	private String jobTempDir;

	@Inject
	@Configuration(key = "backmeup.job.backupname")
	private String backupName;

	public Plugin getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugin plugins) {
		this.plugins = plugins;
	}

	public Keyserver getKeyserver() {
		return keyserver;
	}

	public void setKeyserver(Keyserver keyserver) {
		this.keyserver = keyserver;
	}

	public DataAccessLayer getDal() {
		return dal;
	}

	public void setDal(DataAccessLayer dal) {
		this.dal = dal;
	}

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

	private EntityManagerFactory emFactory;

	private final Logger logger = LoggerFactory
			.getLogger(RabbitMQJobReceiver.class);

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

	public RabbitMQJobReceiver(String mqHost, String mqName) throws IOException {
		this.mqName = mqName;

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
