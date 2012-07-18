package org.backmeup.plugin.api.storage.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.storage.DataObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class FileDataObject implements DataObject {
  private static Gson gson;
  
  static {
    GsonBuilder gsonBuilder = new GsonBuilder();    
    gsonBuilder.registerTypeAdapter(Metainfo.class, new JsonDeserializer<Metainfo>() {
      @Override
      public Metainfo deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        Metainfo meta = new Metainfo();
        for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
          meta.setAttribute(entry.getKey(), entry.getValue().getAsString());
        }
        return meta;
      }
    });
    gson = gsonBuilder.create();
  }
	
	private File file;
	
	private String path;
	
	public FileDataObject(File file, String path) {
		this.file = file;
		this.path = path;
	}

	public byte[] getBytes() throws IOException {
	  // Replaced IOUtils because they returned too many bytes (and I don't know why).
	  InputStream is = new FileInputStream(file);
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  try {
  	  int maxLen = 1024 * 1024;
  	  byte[] buffer = new byte[maxLen];
  	  int len;
  	  while((len = is.read(buffer, 0, maxLen)) > 0) {
  	    baos.write(buffer, 0, len);	    
  	  }
  	  return baos.toByteArray();
	  } finally {
	    baos.close();
	    is.close();
	  }
	}

	public String getPath() {
		return path;
	}

	public String getProperty(String name) {
		// Metadata properties not supported on filesystem
		return null;
	}
	
	@Override
	public String toString() {
		try {
			return path + " (" + getBytes().length + " bytes)";
		} catch (IOException e) {
			return path + " (corrupt file?)";
		}
	}

	public long getLength() {
		return file.length();
	}
	
	public Metainfo getMetainfo() {
	  File metaFile = new File(file.getPath() + ".meta.json");
	  if (metaFile.exists()) {
	    InputStreamReader reader = null;	  
	    try { 
	      reader = new InputStreamReader(new FileInputStream(metaFile));
	      return gson.fromJson(reader, Metainfo.class);
	    } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
	    finally {
	      try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
	    }
	  }
	  return null;
	}

}
