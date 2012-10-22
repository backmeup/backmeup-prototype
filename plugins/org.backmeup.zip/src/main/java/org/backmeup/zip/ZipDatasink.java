package org.backmeup.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class ZipDatasink implements Datasink {

  @Override
  public String upload(Properties accessData, Storage storage,
      Progressable progressor) throws StorageException {
    
    ZipHelper zipHelper = ZipHelper.getInstance();
    String tmpDir = accessData.getProperty ("org.backmeup.tmpdir");
    String userId = accessData.getProperty ("org.backmeup.userid");
    
    if (tmpDir == null) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Error: tmpDir property has not been set!");
    }
    
    if (userId == null) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Error: org.backmeup.userid property has not been set!");
    }
    
    String fileName = tmpDir + "_" + new Date().getTime() +".zip";
    String path = MessageFormat.format(zipHelper.getTemporaryPath(), userId) + fileName;
    
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    try {      
      // create folder to file
      new File(path).getParentFile().mkdirs();
      // create zip file
      fos = new FileOutputStream(path);
      zos = new ZipOutputStream(fos);
      Iterator<DataObject> it = storage.getDataObjects();
      while(it.hasNext()) {
        DataObject entry = it.next();
        String entryPath = entry.getPath();
        if (entryPath.startsWith("/") || entryPath.startsWith("\\"))
          entryPath = entryPath.substring(1);        
        zos.putNextEntry(new ZipEntry(entryPath));
        zos.write(entry.getBytes());
        zos.closeEntry();
      }
      zos.close();
      fos.close();            
      if (zipHelper.isRemote()) {
        zipHelper.sendToSftpDestination(new FileInputStream(path), fileName, userId);
      }
    } catch (Exception ex) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "An exception occurred during zip creation!", ex);
    } finally {
      if (fos != null)
        try {
          fos.close();
        } catch (IOException e) {
          throw new PluginException(ZipDescriptor.ZIP_ID, "An exception occurred during zip creation!", e);
        }
      if (zos != null)
        try {
          zos.close();
        } catch (IOException e) {
          throw new PluginException(ZipDescriptor.ZIP_ID, "An exception occurred during zip creation!", e);
        }
    }
    return null;
  }

}
