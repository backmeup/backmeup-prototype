package org.backmeup.model.spi;

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
}
