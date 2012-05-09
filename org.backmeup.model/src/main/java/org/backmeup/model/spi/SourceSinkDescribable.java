package org.backmeup.model.spi;

/**
 * Datasources and Datasinks must implement this interface.
 * The getType method returns if this plugin 
 * contains a source implementation, a sink implementation or both.
 * 
 * The imageURL returns a link to a thumbnail picture of this plugin.
 * 
 * @author fschoeppl
 *
 */
public interface SourceSinkDescribable extends Describable {
	public enum Type {
		Source,
		Sink,
		Both
	}
	public Type getType();
	public String getImageURL();
}
