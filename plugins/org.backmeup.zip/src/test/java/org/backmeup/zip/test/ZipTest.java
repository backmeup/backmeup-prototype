package org.backmeup.zip.test;

import java.util.Properties;

import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.zip.ZipDatasink;

public class ZipTest {
  public static void main(String[] args) {
    Storage storage = new LocalFilesystemStorage();
    try {
      String tmpDir = "C:/temp/TEST";
      storage.open(tmpDir);
      ZipDatasink sink = new ZipDatasink();
      Properties accessData = new Properties();
      accessData.setProperty ("org.backmeup.tmpdir", getLastSplitElement (tmpDir, "/"));
      accessData.setProperty ("org.backmeup.userid", "1");
      sink.upload(accessData, storage, new Progressable() {        
        @Override
        public void progress(String message) {
          System.out.println(message);
        }
      });      
    } catch (StorageException e) {      
      e.printStackTrace();
    }
  }

  private static String getLastSplitElement (String text, String regex)
  {
    String[] parts = text.split (regex);
    
    if (parts.length > 0)
    {
      return parts[parts.length - 1];
    }
    else
    {
      return text;
    }
  }  
}
