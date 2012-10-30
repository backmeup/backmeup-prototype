package org.backmeup.dummy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class DummyDatasource implements Datasource {
  private InputStream stringToStream(String input) {
    try {
      InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
      return is;
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  private Metainfo create(String id, String type, String destination) {
    Metainfo info = new Metainfo();
    info.setBackupDate(new Date());
    info.setDestination(destination);
    info.setId(id);
    info.setSource("dummy");
    info.setType(type);
    return info;
  }

  @Override
  public void downloadAll(Properties accessData, List<String> options, Storage storage,
      Progressable progressor) throws DatasourceException, StorageException {
    MetainfoContainer cont = new MetainfoContainer();
    cont.addMetainfo(create("1", "text/plain", "/plain.txt"));
    InputStream is = stringToStream("This is an important text file.\nPlease create a backup with this file");
    storage.addFile(is, "/plain.txt", cont);

    cont = new MetainfoContainer();
    cont.addMetainfo(create("2", "text/html", "/html.txt"));
    is = stringToStream("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
        + "http://www.w3.org/TR/html4/strict.dtd\">"
        + "<html>"
        + "<head>"
        + "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"
        + "    <title>title</title>"
        + "</head>"
        + "<body><p>This is one important text file.\nPlease create a backup with this file</p></body></html>");
    storage.addFile(is, "/html.txt", cont);
  }

  @Override
  public String getStatistics(Properties accesssData) {
    return "statistics are empty";
  }

  @Override
  public List<String> getAvailableOptions(Properties accessData) {
    List<String> options = new ArrayList<String>();
    options.add("option1");
    options.add("option2");
    return options;
  }

}
