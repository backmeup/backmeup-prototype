package org.backmeup.rest.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates one single EntityManager for the
 * whole application. It will be injected into the
 * Connection class of the business layer.
 * 
 * @author fschoeppl
 *
 */
public class EntityManagerFactoryProducer {
	private final Logger logger = LoggerFactory.getLogger(EntityManagerFactoryProducer.class); 
	@Produces
	@ApplicationScoped
	public EntityManagerFactory create() {
		return Persistence.createEntityManagerFactory("org.backmeup.jpa");
	}
	
	public void destroy(@Disposes EntityManagerFactory factory) {
	  logger.debug("Closing EntityManagerFactory!");
		factory.close();
	}
}
