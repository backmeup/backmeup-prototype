package org.backmeup.logic.impl.helper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Alternative
public class EntityManagerFactoryProducer {
  
  @Alternative
	@Produces
	@ApplicationScoped
	public EntityManagerFactory create() {
		return Persistence.createEntityManagerFactory("org.backmeup.jpa");
	}
	
	public void destroy(@Disposes EntityManagerFactory factory) {
	  if (factory.isOpen())
	    factory.close();
	}
}
