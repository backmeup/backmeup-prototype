package org.backmeup.dropbox;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.WebAuthSession;

/**
 * The DropboxDataSink class uploads all elements from the StorageReader up to
 * Dropbox based on the token and secret of a certain user.
 * 
 * @author fschoeppl
 * 
 */
public class DropboxDatasink implements Datasink {

	@Override
	public String upload(Properties items, Storage storage,
			Progressable progressor) throws StorageException {
		DropboxAPI<WebAuthSession> api = DropboxHelper.getApi(items);
		Iterator<DataObject> it = storage.getDataObjects();
		while (it.hasNext()) {
			DataObject dataObj = it.next();
			String fileName = dataObj.getPath();
			// just in case the fileName is not formatted as expected
			// change to slashes instead of backslashes.
			fileName = fileName.replace("\\", "/").replace("//", "/");
			
			String tmpDir;
			if (items.containsKey ("org.backmeup.tmpdir") == true)
			{
				tmpDir = items.getProperty ("org.backmeup.tmpdir");
			}
			else
			{
				throw new PluginException (DropboxDescriptor.DROPBOX_ID, "Property \"org.backmeup.tmpdir\" is not set");
			}
			
			if (!fileName.startsWith("/"))
			{
				fileName = "/" +  tmpDir + "/" + fileName;
			}
			else
			{
				fileName = "/" +  tmpDir + fileName;
			}

			try {
				byte[] data = dataObj.getBytes();
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				String log = String.format("Uploading file %s ...", dataObj.getPath());
				progressor.progress(log);
				api.putFile(fileName, bis, data.length, null, null);
				bis.close();
			} catch (Exception e) {
				throw new PluginException(DropboxDescriptor.DROPBOX_ID, String.format(
						"An error occurred during upload of file %s", fileName), e);
			}
		}
		return null;
	}

}
