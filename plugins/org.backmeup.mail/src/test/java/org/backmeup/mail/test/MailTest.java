package org.backmeup.mail.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.backmeup.mail.MailDatasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;

public class MailTest {
  public static void main(String[] args) throws Exception {
 // Use the properties saved during DropboxAuthenticate to download all files from Dropbox
    Properties props = new Properties();
    props.load(MailTest.class.getClassLoader().getResourceAsStream("auth.props"));    
    
    MailDatasource source = new MailDatasource();
    Storage storage = new LocalFilesystemStorage();
    storage.open("C:/TEMP/TEST/");
    source.downloadAll(props, new ArrayList<String>(), storage, new Progressable() {
      @Override
      public void progress(String message) {}
    });
    
    // StorageReader sr = new LocalFilesystemStorageReader();
    // sr.open("C:/TEMP/TEST/");
    Iterator<DataObject> it = storage.getDataObjects();
    while (it.hasNext()) {
      DataObject da = it.next();
      System.out.println(da.getMetainfo());
      System.out.println();
    }
  }
}
