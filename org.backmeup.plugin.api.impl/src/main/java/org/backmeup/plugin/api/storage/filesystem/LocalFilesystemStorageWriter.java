package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.apache.hadoop.hdfs.server.common.Storage;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * An implementation of {@link Storage} based on a directory on the local file
 * system.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class LocalFilesystemStorageWriter extends StorageWriter {
  private static Gson gson;
  
  static {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Metainfo.class, new JsonSerializer<Metainfo>(){
      @Override
      public JsonElement serialize(Metainfo src, Type typeOfSrc,
          JsonSerializationContext context) {
        JsonObject info = new JsonObject();
        for (Entry<Object, Object> entry : src.getAttributes().entrySet()) {
          String key = (String)  entry.getKey();
          String value = (String) entry.getValue();
          info.addProperty(key, value);
        }
        if (src.getParent() != null)
          info.addProperty("parent", src.getParent());
        return info;
      }
    });
    gson = gsonBuilder.create();
  }
  
	private File directory;
	
	@Override
	public void open(String path) {
		this.directory = new File(path);
		if (!this.directory.exists())
			this.directory.mkdir();
	}
	
	@Override
	public void addFile(InputStream is, String path, Metainfo metadata) throws StorageException {
		try {
			File out = new File(directory, path);
			out.getParentFile().mkdirs();
			
			OutputStream os = new FileOutputStream(out);
						
			byte buf[] = new byte[1024];
			int len;
			while((len = is.read(buf)) > 0)
				os.write(buf, 0, len);
			
			os.close();
			is.close();
			if (metadata != null) {
			  byte[] json = gson.toJson(metadata).getBytes("UTF-8");
			  File metaFile = new File(directory, path + ".meta.json");
			  os = new FileOutputStream(metaFile);
			  os.write(json, 0, json.length);
			  os.close();
			}
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}
	
	@Override
	public void close() throws StorageException {
		// Do nothing
	}

  @Override
  public void addFile(InputStream is, String path)
      throws StorageException {
    addFile(is, path, null);
  }

}
