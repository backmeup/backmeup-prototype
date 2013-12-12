package org.backmeup.rest.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JNDIBeanManager {
	private static final Logger logger = LoggerFactory.getLogger(JNDIBeanManager.class);
	
	private final static String JNDI_NAME_1 = "java:comp/BeanManager";
	private final static String JNDI_NAME_2 = "java:comp/env/BeanManager";
	private final static String JNDI_NAME_3 = "java:comp/app/BeanManager";
	
	private BeanManager beanManager;
	
	public static JNDIBeanManager getInstance() {
		JNDIBeanManager jndiManager = new JNDIBeanManager();
		if (jndiManager.beanManager == null) {
			try {
				InitialContext context = new InitialContext();
				jndiManager.beanManager = (BeanManager) context.lookup(JNDI_NAME_1);
			} catch (Exception e) {
				logger.debug("BeanManager under JNDI name {} not found",JNDI_NAME_1,  e);
			}
		}
	
		if(jndiManager.beanManager == null){
			try {
				InitialContext context = new InitialContext();
				jndiManager.beanManager = (BeanManager) context.lookup(JNDI_NAME_2);
			} catch (Exception e) {
				logger.debug("BeanManager under JNDI name {} not found",JNDI_NAME_2,  e);
			}
		}
		
		if(jndiManager.beanManager == null){
			try {
				InitialContext context = new InitialContext();
				jndiManager.beanManager = (BeanManager) context.lookup(JNDI_NAME_3);
			} catch (Exception e) {
				logger.debug("BeanManager under JNDI name {} not found",JNDI_NAME_3,  e);
			}
		}
		
		if(jndiManager.beanManager != null){
			logger.debug("BeanManger successfully located");
			return jndiManager;
		} else {
			throw new RuntimeException("BeanManager cannot be found");
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> type) {
		Bean<T> bean = (Bean<T>)beanManager.resolve(beanManager.getBeans(type));
		CreationalContext<T> creationalContext = beanManager.createCreationalContext(bean);
		return (T) beanManager.getReference(bean, type, creationalContext);
	}

}
