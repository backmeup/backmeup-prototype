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
}
