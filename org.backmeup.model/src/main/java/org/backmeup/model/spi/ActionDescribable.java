package org.backmeup.model.spi;

import java.util.List;

/**
 * Action plugins which will be activated during the backup process 
 * (Download from Datasource -> Action -> Upload to Datasink)
 * must extend this interface.
 * 
 * @author fschoeppl
 *
 */
public interface ActionDescribable extends Describable {
	public int getPriority();
	
	public List<String> getAvailableOptions ();
	
	public String getActionVisibility();
	
	// can only be configured globally, not being shown during the creation of a job
	public static final String VISIBILITY_GLOBAL = "global";
	// only be shown during the creation of the job
	public static final String VISIBILITY_JOB = "job";
	// not being shown to the user
	public static final String VISIBILITY_HIDDEN = "hidden";
	
}
