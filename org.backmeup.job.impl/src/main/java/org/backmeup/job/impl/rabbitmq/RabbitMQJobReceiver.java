package org.backmeup.job.impl.rabbitmq;

import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.osgi.PluginImpl;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Experimental. An alternative to Hadoop-based clustering, based on a simple
 * standalone Java app, picking up jobs via RabbitMQ.
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
	
	public static void main(String[] args) {
		System.out.println("Starting...");
	}
	
	public RabbitMQJobReceiver(String pluginsDir, String osgiTempDir) {
		// Start up the Plugin manager
		this.plugins = new PluginImpl(pluginsDir, osgiTempDir, EXPORTED_PACKAGES);
		this.plugins.startup();
	    ((PluginImpl)plugins).waitForInitialStartup();
	}
	
	public class BackupJobWorker implements Runnable {
		
		public BackupJobWorker() {
			ConnectionFactory factory = new ConnectionFactory();
			
		}

		@Override
		public void run() {
			System.out.println("Querying the job queue");
			
			
		}
		
	}

}
