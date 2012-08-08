package org.backmeup.mail.test;

import java.util.Iterator;
import java.util.Properties;

import org.backmeup.mail.MailDatasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;

public class MailTest {
  public static void main(String[] args) throws Exception {
 // Use the properties saved during DropboxAuthenticate to download all files from Dropbox
    Properties props = new Properties();
    props.load(MailTest.class.getClassLoader().getResourceAsStream("auth.props"));    
    
    MailDatasource source = new MailDatasource();
    StorageWriter sw = new LocalFilesystemStorageWriter();
    sw.open("C:/TEMP/TEST/");
    source.downloadAll(props, sw, new Progressable() {
      @Override
      public void progress(String message) {}
    });
    
    StorageReader sr = new LocalFilesystemStorageReader();
    sr.open("C:/TEMP/TEST/");
    Iterator<DataObject> it = sr.getDataObjects();
    while (it.hasNext()) {
      DataObject da = it.next();
      System.out.println(da.getMetainfo());
      System.out.println();
    }
  }
}
