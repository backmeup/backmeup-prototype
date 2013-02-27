package org.backmeup.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class ZipDatasink implements Datasink {
  private Logger logger = Logger.getLogger(ZipDatasink.class.getName());
  
  @Override
  public String upload(Properties accessData, Storage storage,
      Progressable progressor) throws StorageException {
	  
    ZipHelper zipHelper = ZipHelper.getInstance();
    String tmpDir = accessData.getProperty ("org.backmeup.tmpdir");
    String userId = accessData.getProperty ("org.backmeup.userid");
    
    if (tmpDir == null) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Error: org.backmeup.tmpDir property has not been set!");
    }
    
    if (userId == null) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Error: org.backmeup.userid property has not been set!");
    }
    
    String fileName = tmpDir + "_" + new Date().getTime() +".zip";
    logger.log(Level.FINE, "Creating zip backup file: " + fileName);
    String path = MessageFormat.format(zipHelper.getTemporaryPath(), userId) + fileName;
    logger.log(Level.FINE, "Path zip backup path: " + path);
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    
    try {
    	System.out.println ("Generate temp folders");
      // create folder to file
      new File(path).getParentFile().mkdirs();
      // create zip file
      fos = new FileOutputStream(path);
      zos = new ZipOutputStream(fos);
      zos.setEncoding ("UTF-8");
      Iterator<DataObject> it = storage.getDataObjects();
      while(it.hasNext()) {
    	  System.out.println ("Get entry");
        DataObject entry = it.next();
        
        System.out.println ("Get path");
        String entryPath = entry.getPath();
        System.out.println ("Path: " + entryPath);
        
        System.out.println ("Replace slashes");
        if (entryPath.startsWith("/") || entryPath.startsWith("\\"))
          entryPath = entryPath.substring(1);
        
        System.out.println ("Log something");
        logger.log(Level.FINE, "Putting entry to zip: " + entryPath);
        
        System.out.println ("Put entry to zos");
        zos.putNextEntry(new ZipEntry(entryPath));
        
        System.out.println ("Write entry");
        zos.write(entry.getBytes());
        
        System.out.println ("Close entry");
        zos.closeEntry();
      }
      logger.log(Level.FINE, "Zip file created.");
      zos.close();
      fos.close();            
      if (zipHelper.isRemote()) {
        logger.log(Level.FINE, "Sending zip file to sftp destination...");
        InputStream stream = null;
        try {
          stream = new FileInputStream(path);
          zipHelper.sendToSftpDestination(stream, fileName, userId);
        } finally {
          try {
            if (stream != null)
              stream.close();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    } catch (Exception ex) {
    	ex.printStackTrace ();
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
