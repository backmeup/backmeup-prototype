package org.backmeup.configuration.cdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationFactory {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);
	private static final String mandatoryKeyMissing ="No definition found for mandatory configuration property '{0}'";
	private static final String propertiesFileName = "backmeup.properties";
	private static final String propertiesFilePath = "config/"+ propertiesFileName;
	
	private static volatile Properties properties;

	public synchronized static Properties getProperties() {
		if (properties == null) {
			logger.debug("Locate configuration properties file");
			
			InputStream is = ConfigurationFactory.class.getClassLoader().getResourceAsStream(propertiesFileName);

			if (is == null) {
				logger.debug("No properties file ({}) in classpath found",propertiesFileName);

				try {
					is = new FileInputStream(new File(propertiesFilePath));
				} catch (FileNotFoundException e) {
					logger.debug("No properties file found at path: {}",propertiesFilePath);
				}
			}

			if (is == null) {
				logger.error("No properties file found. Add {} or add it to the classpath!", propertiesFilePath);
				throw new RuntimeException("No properties file found");
			}
			
			properties = new Properties();
			try {
				properties.load(is);
			} catch (IOException e) {
				logger.error("Failed to load properties file. Add {} or add it to the classpath!", propertiesFilePath);
				throw new RuntimeException("Failed to load properties file");
			}
		}
		return properties;
	}
	
	@Produces
	@Configuration
	public String getConfiguration(InjectionPoint ip){
		Configuration param = ip.getAnnotated().getAnnotation(Configuration.class);
		if(param.key() == null || param.key().length() == 0){
			logger.debug("Configuration parameter null or empty, returning default value");
			return param.defaultValue();
		}
		
		Properties config = getProperties();
		String value = config.getProperty(param.key());
		
		if(value == null){
			logger.debug("No definition found for config parameter '{}'", param.key());
			if(param.mandatory()){
				throw new IllegalStateException(MessageFormat.format(mandatoryKeyMissing, new Object[]{param.key()}));
			} else {
				logger.debug("Returning default value for mandatory config parameter '{}'", param.key());
				return param.defaultValue();
			}
		}
		logger.info("Configuration: key='{}' value='{}'", param.key(), value);
		return value;
	}
	
	@Produces
	@Configuration
	public Integer getConfigurationInt(InjectionPoint ip) {
		String value = getConfiguration(ip);
		return Integer.parseInt(value);
	}
	
	@Produces
	@Configuration
	public Double getConfigurationDouble(InjectionPoint ip) {
		String value = getConfiguration(ip);
		return Double.parseDouble(value);
	}
	
	@Produces
	@Configuration
	public Boolean getConfigurationBoolean(InjectionPoint ip) {
		String value = getConfiguration(ip);
		return Boolean.parseBoolean(value);
	}
}
