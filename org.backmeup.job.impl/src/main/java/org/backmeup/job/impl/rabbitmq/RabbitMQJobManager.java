package org.backmeup.job.impl.rabbitmq;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.job.impl.AkkaJobManager;
import org.backmeup.model.BackupJob;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * An implementation of {@link AkkaJobManager} that pushes backup jobs into 
 * a RabbitMQ queue, where they can be handled by worker nodes.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@ApplicationScoped
public class RabbitMQJobManager extends AkkaJobManager {
	private final Logger logger = LoggerFactory.getLogger(RabbitMQJobManager.class);
	
	@Inject
	@Configuration(key="backmeup.message.queue.host")
	private String mqHost;
	
	@Inject
	@Configuration(key="backmeup.message.queue.name")
	private String mqName;
	
	private Connection mqConnection;
	
	private Channel mqChannel;
	/*
	public RabbitMQJobManager() throws IOException {
		//init();
	}
	*/
	/*
	RabbitMQJobManager(String mqHost, String mqName) throws IOException {
		this.mqHost = mqHost;
		this.mqName = mqName;
		init();
	}
	*/
	
	@Override
	public void start() {
	  super.start();
	  try {
      init();
    } catch (IOException e) {     
      //TODO: Log or rethrow Exception
      throw new BackMeUpException(e);
    }
	}
	
	private void init() throws IOException {
		// Setup connection to the message queue
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
		
		mqConnection = factory.newConnection();
		mqChannel = mqConnection.createChannel();
		mqChannel.queueDeclare(mqName, false, false, false, null);		
	}

	@Override
	public void shutdown() {
		super.shutdown();
		try {
			mqChannel.close();
			mqConnection.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void runJob(BackupJob job) {
		try {
		  conn.beginOrJoin();
		  // we need a JPA-managed instance
		  BackupJob job2 = dal.createBackupJobDao().findById(job.getId());
			logger.info("Sending job to processing queue: " + job2.getId());
			String json = JsonSerializer.serialize(job2);
			mqChannel.basicPublish("", mqName, null, json.getBytes());
		} catch (IOException e) {
			// Should only happen if message queue is down
			logger.error("message queue down", e);
			throw new RuntimeException(e);
		} finally {
		  conn.rollback();
		}
	}

}
