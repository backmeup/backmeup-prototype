package org.backmeup.dummy;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class DummyDatasink implements Datasink {

  @Override
  public String upload(Properties accessData, Storage storage,
      Progressable progressor) throws StorageException {
	
	System.out.println("Uploading to StorageReader");
	  
    Iterator<DataObject> it = storage.getDataObjects();
    while (it.hasNext()) {
      DataObject obj = it.next();
      Iterator<Metainfo> infos = obj.getMetainfo().iterator();
      if (infos.hasNext()) {
        System.out.println("=============================================");
        System.out.println("Metainfos of object:\t\t" + obj.getPath());
        while (infos.hasNext()) {
          Metainfo info = infos.next();
          for (Entry<Object, Object> entry : info.getAttributes().entrySet()) {
            System.out.println(entry.getKey() + ":\t\t" + entry.getValue());
          }
        }
      }
    }
    return "not used";
  }

}
