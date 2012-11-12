package org.backmeup.dropbox;

import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxValidator implements Validationable {
	public ValidationNotes validate(Properties accessData) {
		ValidationNotes notes = new ValidationNotes();
		try {
			// 1. Make sure authentication / authorization is working well
			DropboxAPI<WebAuthSession> api = DropboxHelper.getApi(accessData);
			if (!api.getSession().isLinked()) {
				notes.addValidationEntry(ValidationExceptionType.AuthException, DropboxDescriptor.DROPBOX_ID);			
			}			
			
			// 2. Crawl metadata via the API so that we can be sure that the API is working as expected.
			// Note: This does not ensure that all API calls work.
			Entry entry = api.metadata("/", 100, null, true, null);
			entry.contents.size();									
		} catch (DropboxUnlinkedException due) {
			notes.addValidationEntry(ValidationExceptionType.AuthException, DropboxDescriptor.DROPBOX_ID, due);
		}
		catch (DropboxException de) {
			notes.addValidationEntry(ValidationExceptionType.APIException, DropboxDescriptor.DROPBOX_ID, de);
		}
		return notes;
	}
}
