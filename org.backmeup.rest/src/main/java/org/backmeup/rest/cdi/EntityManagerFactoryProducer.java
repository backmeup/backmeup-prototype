package org.backmeup.rest.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * This class creates one single EntityManager for the
 * whole application. It will be injected into the
 * Connection class of the business layer.
 * 
 * @author fschoeppl
 *
 */
public class EntityManagerFactoryProducer {
	@Produces
	@ApplicationScoped
	public EntityManagerFactory create() {
		return Persistence.createEntityManagerFactory("org.backmeup.jpa");
	}
	
	public void destroy(@Disposes EntityManagerFactory factory) {
		factory.close();
	}
}
