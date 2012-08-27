package org.backmeup.job.impl.rabbitmq;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQJobTest {
	
	private static final String MQ_HOST = "localhost";
	
	private static final String MQ_NAME = "test-queue";
	
	private static final String PLUGINS_DIR = "/home/simonr/Workspaces/backmeup/backmeup-prototype/org.backmeup.embedded/autodeploy";
	
	private static final String OSGI_TEMP_DIR = "/home/simonr/Workspaces/backmeup/backmeup-prototype/osgi-tmp";
	
	private static final String BACKUP_JOB =
			"{\"user\":{\"userId\":1,\"username\":\"Sepp\",\"password\":\"pw\"," + 
	        "\"keyRing\":\"k3yr1nG\",\"email\":\"e@ma.il\",\"isActivated\":false,\"properties\":[]}," +
			"\"sourceProfiles\":" +
			"[{\"profile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"Sepp\"," +
			"\"password\":\"pw\",\"keyRing\":\"k3yr1nG\",\"email\":\"e@ma.il\",\"isActivated\":" +
			"false,\"properties\":[]},\"profileName\":\"TestProfile\",\"desc\":" +
			"\"org.backmeup.dummy\",\"sourceAndOrSink\":\"Source\"},\"options\":" + 
			"[\"folder1\",\"folder2\"]}]," +
			"\"sinkProfile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"Sepp\"" +
			",\"password\":\"pw\",\"keyRing\":\"pw\",\"email\":\"e@ma.il\",\"isActivated\":" +
			"false,\"properties\":[]},\"profileName\":\"TestProfile2\",\"desc\":" +
			"\"org.backmeup.dummy\",\"sourceAndOrSink\":\"Sink\"},\"requiredActions\":[]," + 
			"\"start\":\"1345203377704\",\"delay\":1345203258212}";
	
	private Connection mqConnection;
	
	private Channel mqChannel;
	
	private RabbitMQJobReceiver mqRecevier;
	
	private boolean fRabbitMQInstalled = true;
	
	@Before
	public void setUp() throws Exception {
		// Setup connection to the message queue
		System.out.println("Connecting test sender to message queue");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(MQ_HOST);
		
		try {
			mqConnection = factory.newConnection();
			mqChannel = mqConnection.createChannel();
			mqChannel.queueDeclare(MQ_NAME, false, false, false, null);		
		} catch (ConnectException e) {
			System.out.println("WARNING: RabbitMQ not installed or shutdown");
			fRabbitMQInstalled = false;
		}
	}
	
	@Test
	public void testRabbitMQJobExecution() throws IOException, InterruptedException {	
		if (fRabbitMQInstalled) {
			// Set up a receiver
			mqRecevier = new RabbitMQJobReceiver(MQ_HOST, MQ_NAME, PLUGINS_DIR, OSGI_TEMP_DIR);
			mqRecevier.start();
			
			// Send job into the queue
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
						mqChannel.basicPublish("", MQ_NAME, null, BACKUP_JOB.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				}
			});
			t.start();
			
			// Wait for 10 seconds
			Thread.sleep(5000);
			System.out.println("Completing test");
		} else {
			System.out.println("Skipping RabbitMQ test");
		}
	}
	
	@After
	public void tearDown() throws IOException {
		if (fRabbitMQInstalled) {
			mqRecevier.stop();
			mqChannel.close();
			mqConnection.close();
		}
		
	    FileUtils.deleteDirectory(new File(OSGI_TEMP_DIR));
	}

}
