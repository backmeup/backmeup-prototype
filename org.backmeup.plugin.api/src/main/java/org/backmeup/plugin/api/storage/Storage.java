package org.backmeup.plugin.api.storage;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map.Entry;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public abstract class Storage {

	protected Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(MetainfoContainer.class, new JsonSerializer<MetainfoContainer>() {
			
			@Override
			public JsonElement serialize(MetainfoContainer src, Type typeOfSrc, 
					JsonSerializationContext context) {
				
				JsonArray array = new JsonArray();
		        for (Metainfo info : src) {
		        	JsonObject metainfoEntry = new JsonObject();
		        	
		        	for (Entry<Object, Object> entry : info.getAttributes().entrySet()) {
		        		String key = (String)  entry.getKey();
		        		String value = (String) entry.getValue();
		        		metainfoEntry.addProperty(key, value);
		        	}
		          
		        	if (info.getParent() != null)
		        		metainfoEntry.addProperty("parent", info.getParent());
		        	
		        	array.add(metainfoEntry);
		        }
		        
		        return array;
		      }
		    });
		
		return gsonBuilder.create();
	}

	public abstract void open(String path) throws StorageException;
	
	public abstract void close() throws StorageException;	
	
	public abstract void delete() throws StorageException;
	
	/** Read methods **/
	
	public abstract int getDataObjectCount() throws StorageException;
	
	public abstract Iterator<DataObject> getDataObjects() throws StorageException;
	
	/** Write methods **/
	
	public abstract void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException;
	
	public abstract void removeFile(String path) throws StorageException;
	
	public abstract void moveFile(String fromPath, String toPath) throws StorageException;
	
}
