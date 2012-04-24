package org.backmeup.model.spi;

public interface SourceSinkDescribable extends Describable {
	public enum Type {
		Source,
		Sink,
		Both
	}
	public Type getType();
	public String getImageURL();
}
