package org.backmeup.dropbox;

import org.backmeup.model.spi.SourceSinkDescribable;

public class DropboxDescriptor implements SourceSinkDescribable {

	@Override
	public String getId() {
		return "org.backmeup.dropbox";
	}

	@Override
	public String getTitle() {
		return "BackMeUp Dropbox Plug-In";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of downloading and uploading from dropbox";
	}

	@Override
	public String getImageURL() {
		return "http://about:blank";
	}

	@Override
	public Type getType() {
		return Type.Source;
	}

}
