package org.backmeup.embedded;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.backmeup.configuration.Configuration;
import org.backmeup.job.impl.rabbitmq.RabbitMQJobReceiver;
import org.backmeup.rest.Actions;
import org.backmeup.rest.BackupJobs;
import org.backmeup.rest.Backups;
import org.backmeup.rest.Datasinks;
import org.backmeup.rest.Datasources;
import org.backmeup.rest.Mails;
import org.backmeup.rest.Profiles;
import org.backmeup.rest.Users;
import org.backmeup.rest.exceptionmapper.AlreadyRegisteredExceptionMapper;
import org.backmeup.rest.exceptionmapper.BackMeUpExceptionMapper;
import org.backmeup.rest.exceptionmapper.IllegalArgumentExceptionMapper;
import org.backmeup.rest.exceptionmapper.InvalidCredentialsMapper;
import org.backmeup.rest.exceptionmapper.NullPointerExceptionMapper;
import org.backmeup.rest.exceptionmapper.UnknownUserExceptionMapper;
import org.backmeup.rest.provider.ObjectMapperContextResolver;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

/**
 * The Main class starts the TJWS embedded server of RESTeasy with our REST api
 * and backend. The server will be started on port 8080.
 * 
 * @author fschoeppl
 * 
 */
public class Main {
	private static final int PORT = 8080;
	
	private static Node indexNode = NodeBuilder.nodeBuilder().local(false).node();		

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(PORT).build();
	}

	public static final URI BASE_URI = getBaseURI();

	public static TJWSEmbeddedJaxrsServer startServer() {
		TJWSEmbeddedJaxrsServer tjws = new TJWSEmbeddedJaxrsServer();
		tjws.setPort(PORT);
		List<String> classes = new ArrayList<String>();
		classes.add(Actions.class.getName());
		classes.add(BackupJobs.class.getName());
		classes.add(Backups.class.getName());
		classes.add(Datasinks.class.getName());
		classes.add(Datasources.class.getName());
		classes.add(Users.class.getName());
		classes.add(Profiles.class.getName());
		classes.add(Mails.class.getName());
		classes.add(org.backmeup.rest.Metadata.class.getName());
		tjws.getDeployment().getResourceClasses().addAll(classes);
		tjws.getDeployment().getProviderClasses()
				.add(AlreadyRegisteredExceptionMapper.class.getName());
		tjws.getDeployment().getProviderClasses()
				.add(IllegalArgumentExceptionMapper.class.getName());
		tjws.getDeployment().getProviderClasses()
				.add(InvalidCredentialsMapper.class.getName());
		tjws.getDeployment().getProviderClasses()
				.add(NullPointerExceptionMapper.class.getName());
		tjws.getDeployment().getProviderClasses()
				.add(UnknownUserExceptionMapper.class.getName());
		tjws.getDeployment().getProviderClasses()
				.add(BackMeUpExceptionMapper.class.getName());
		tjws.getDeployment().getProviderClasses()
				.add(ObjectMapperContextResolver.class.getName());
		Hashtable<String, String> ctxParams = new Hashtable<String, String>();
		ctxParams.put("resteasy.resources",
				ObjectMapperContextResolver.class.getName());
		tjws.setContextParameters(ctxParams);
		tjws.start();
		return tjws;
	}
	
	public static Client startIndexClient() {
		return indexNode.client();
	}
	
	public static RabbitMQJobReceiver startRabbitMQWorker() throws IOException {
		File autodeploy = new File("autodeploy");
		RabbitMQJobReceiver rec = new RabbitMQJobReceiver(Configuration.getConfig().getProperty("message.queue.host"), Configuration.getConfig().getProperty("message.queue.name"), autodeploy.getAbsolutePath());
		rec.start();
		return rec;
	}

	public static void main(String[] args) throws Exception {
		String[] items = System.getProperty("java.class.path").split(";");
		System.out.println("Classpath: ");
		Arrays.sort(items);
		for (String itm : items) {
			System.out.println("\t" + itm);
		}

		TJWSEmbeddedJaxrsServer tjws = startServer();	
		System.out.println(String.format("BackMeUp REST Server started at %s", BASE_URI));
		RabbitMQJobReceiver receiver = startRabbitMQWorker();
		System.out.println("RabbitMQWorker running");
		Client indexClient = startIndexClient();
		System.out.println("ElasticSearch index running at http://localhost:9200/");
		System.out.println("Hit enter to stop...");
		
		System.in.read();
		tjws.stop();
		indexClient.close();
		receiver.stop();
		System.exit(0);
	}

}
