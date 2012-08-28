package org.backmeup.job.impl.rabbitmq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class RabbitMQConfig {
	
	private Properties loadProperties() {
	    Properties props = new Properties();
	    InputStream is = null;
	    try {
	      is = getClass().getClassLoader().getResourceAsStream("bl.properties");
	      props.load(is);           
	    } catch (Exception e) {
	    } finally {
	      if (is != null)
	        try {
	          is.close();
	        } catch (IOException  e) {
	          e.printStackTrace();
	        }
	    }
	    return props;
	  }
  
  @Produces
  @Named("message.queue.host")
  public String getHost() {
    return this.loadProperties().getProperty("message.queue.host");
  }
  
  @Produces
  @Named("message.queue.name")
  public String getName() {
    return this.loadProperties().getProperty("message.queue.name");
  }

}

