package org.backmeup.embedded;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.backmeup.rest.Actions;
import org.backmeup.rest.BackupJobs;
import org.backmeup.rest.Backups;
import org.backmeup.rest.Datasinks;
import org.backmeup.rest.Datasources;
import org.backmeup.rest.Users;
import org.backmeup.rest.exceptionmapper.AlreadyRegisteredExceptionMapper;
import org.backmeup.rest.exceptionmapper.IllegalArgumentExceptionMapper;
import org.backmeup.rest.exceptionmapper.InvalidCredentialsMapper;
import org.backmeup.rest.exceptionmapper.NullPointerExceptionMapper;
import org.backmeup.rest.exceptionmapper.UnknownUserExceptionMapper;
import org.backmeup.rest.provider.ObjectMapperContextResolver;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

/**
 * The Main class starts the TJWS embedded server of RESTeasy with 
 * our REST api and backend.
 * The server will be started on port 8080.
 * 
 * @author fschoeppl
 *
 */
public class Main {
  private static final int PORT = 8080;
  
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
    tjws.getDeployment().getResourceClasses().addAll(classes);
    tjws.getDeployment().getProviderClasses().add(AlreadyRegisteredExceptionMapper.class.getName());
    tjws.getDeployment().getProviderClasses().add(IllegalArgumentExceptionMapper.class.getName());
    tjws.getDeployment().getProviderClasses().add(InvalidCredentialsMapper.class.getName());
    tjws.getDeployment().getProviderClasses().add(NullPointerExceptionMapper.class.getName());
    tjws.getDeployment().getProviderClasses().add(UnknownUserExceptionMapper.class.getName());
    tjws.getDeployment().getProviderClasses().add(ObjectMapperContextResolver.class.getName());
    Hashtable<String, String> ctxParams = new Hashtable<String, String>();
    ctxParams.put("resteasy.resources", ObjectMapperContextResolver.class.getName());
    tjws.setContextParameters(ctxParams);
    tjws.start();
    return tjws;
	}

	public static void main(String[] args) throws Exception {
	  String[] items = System.getProperty("java.class.path").split(";");
    System.out.println("Classpath: ");
    Arrays.sort(items);
    for (String itm : items) {
      System.out.println("\t" + itm);
    }
	  
	  System.out.println(String.format("BackMeUp REST Server started:"
        + "\nTry out %s\nHit enter to stop it...", BASE_URI));
	  TJWSEmbeddedJaxrsServer tjws = startServer();
	  System.in.read();
	  tjws.stop();
		System.exit(0);
	}
}
