package org.backmeup.configuration.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
	/**
	 * Bundle key
	 * @return a valid bundle key or ""
	 */
	@Nonbinding 
	String key() default "";
	
	/**
	 * Mandatory property
	 * @return true if it is a mandatory property
	 */
	@Nonbinding 
	boolean mandatory() default false;
	
	/**
	 * Default value
	 * @return default value or ""
	 */
	@Nonbinding 
	String defaultValue() default "";
}
