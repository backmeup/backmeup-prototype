package org.backmeup.grizzly;

import java.net.URI;
import java.util.Arrays;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class Main {
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	private static HttpServer startServer() throws Exception {
		System.out.println("Starting grizzly...");
		ResourceConfig rc = new PackagesResourceConfig("org.backmeup.rest",
				"org.codehaus.jackson.jaxrs");
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	public static void main(String[] args) throws Exception {	
		String[] items = System.getProperty("java.class.path").split(";");
		System.out.println("Classpath: ");
		Arrays.sort(items);
		for (String itm : items) {
			System.out.println("\t" + itm);
		}

		HttpServer httpServer = startServer();
		System.out.println(String.format("BackMeUp REST Server started:"
				+ "\nTry out %s\nHit enter to stop it...", BASE_URI));
		System.in.read();
		httpServer.stop();
		System.exit(0);
	}
}
