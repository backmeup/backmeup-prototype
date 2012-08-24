package org.backmeup.job.impl.rabbitmq;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.backmeup.job.impl.AkkaJobManager;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * An implementation of {@link AkkaJobManager} implementation that pushes
 * backup jobs into a RabbitMQ queue, where they can be handled by worker
 * nodes.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@ApplicationScoped
public class RabbitMQJobManager extends AkkaJobManager {
	
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
	
	public RabbitMQJobManager(String mqHost, String mqName) throws IOException {
		this.mqName = mqName;
		
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
	protected Runnable newJobRunner(final BackupJob job) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Sending job to processing queue: " + job.getId());
					String json = JsonSerializer.serialize(job);
					mqChannel.basicPublish("", mqName, null, json.getBytes());
				} catch (IOException e) {
					// Should only happen if message queue is down
					log.fatal(e.getMessage() + " - message queue down?");
					throw new RuntimeException(e);
				}
			}
		};
	}

}
