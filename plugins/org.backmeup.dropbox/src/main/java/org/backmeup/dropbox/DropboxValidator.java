package org.backmeup.dropbox;

import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.Validationable;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxValidator implements Validationable {
	public ValidationNotes validate(Properties accessData) {
		ValidationNotes notes = new ValidationNotes();
		try {
			// 1. Make sure authentication / authorization is working well
			DropboxAPI<WebAuthSession> api = DropboxHelper.getApi(accessData);
			if (!api.getSession().isLinked()) {
				notes.addValidationEntry(ValidationException.ValidationExceptionType.AuthException, "Invalid access tokens!");			
			}			
			
			// 2. Crawl metadata via the API so that we can be sure that the API is working as expected.
			// Note: This does not ensure that all API calls work.
			Entry entry = api.metadata("/", 100, null, true, null);
			entry.contents.size();									
		} catch (DropboxException de) {
			notes.addValidationEntry(ValidationException.ValidationExceptionType.APIException, "Dropbox API metadata-call failed");
		}
		return notes;
	}
}
