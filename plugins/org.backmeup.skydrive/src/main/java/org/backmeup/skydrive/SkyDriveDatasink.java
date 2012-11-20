package org.backmeup.skydrive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.skydrive.internal.SkyDriveSupport;
import org.backmeup.skydrive.internal.SkyDriveSupport.Service;
/**
 * The SkydriveDatasink class uploads all elements from the StorageReader up to
 * Skydrive based on the token and secret of a certain user.
 * 
 * The upload operation will be delegated to SkyDriveSupport.storeFile.
 * 
 * @author fschoeppl
 * 
 */
public class SkyDriveDatasink implements Datasink {
	@Override
	public String upload(Properties accessData, Storage storage, Progressable progressor) throws StorageException {
		Service s = SkyDriveSupport.getService(accessData);
		String tmpDir = accessData.getProperty("org.backmeup.tmpdir");
		if (tmpDir == null)
		  tmpDir = "";
		Iterator<DataObject> it = storage.getDataObjects();		
		int i = 1;
		while(it.hasNext()) {
			DataObject dataObj = it.next();
			
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(dataObj.getBytes());
				String log = String.format("Uploading file %s (Number: %d)...", dataObj.getPath(), i++);
				progressor.progress(log);				
				SkyDriveSupport.storeFile(s.service, s.accessToken, bis, tmpDir + "/" + dataObj.getPath());
				bis.close();
			} catch (IOException e) {
				throw new PluginException(SkyDriveDescriptor.SKYDRIVE_ID, "Error during upload of file %s", e);
			}
		}
		return null; //SkyDriveSupport.getSharedLink(s.service, s.accessToken, storage.getPath());
	}

}
