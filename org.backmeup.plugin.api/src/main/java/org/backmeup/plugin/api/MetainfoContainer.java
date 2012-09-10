package org.backmeup.plugin.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MetainfoContainer implements Iterable<Metainfo> {
	
	private static Gson gson;
	
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(MetainfoContainer.class, new MetainfoSerializer());
		gson = builder.create();
	}
	
	private List<Metainfo> metainfo = new ArrayList<Metainfo>();

	public void addMetainfo(Metainfo info) {
		this.metainfo.add(info);
	}

	public void removeMetainfo(Metainfo info) {
		this.metainfo.remove(info);
	}

	@Override
	public Iterator<Metainfo> iterator() {
		return metainfo.iterator();
	}

	public Metainfo get(int index) {
		return index < metainfo.size() ? metainfo.get(index) : null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Metainfo m : metainfo) {
			sb.append(m.toString());
		}
		return sb.toString();
	}
	
	public static String toJSON(MetainfoContainer container) {
		return gson.toJson(container);
	}
	
	public static MetainfoContainer fromJSON(String json) {
		return gson.fromJson(json, MetainfoContainer.class);
	}
	
	/** JSON (de)serialization **/

	private static class MetainfoSerializer implements JsonSerializer<MetainfoContainer>,
		JsonDeserializer<MetainfoContainer> {

		@Override
		public MetainfoContainer deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {

			MetainfoContainer c = new MetainfoContainer();
			for (JsonElement element : json.getAsJsonArray()) {
				for (Entry<String, JsonElement> entry : element
						.getAsJsonObject().entrySet()) {
					Metainfo metainfo = new Metainfo();
					metainfo.setAttribute(entry.getKey(), entry.getValue()
							.getAsString());
					c.addMetainfo(metainfo);
				}
			}
			return c;
		}

		@Override
		public JsonElement serialize(MetainfoContainer src, Type typeOfSrc,
				JsonSerializationContext context) {

			JsonArray array = new JsonArray();
			for (Metainfo info : src) {
				JsonObject metainfoEntry = new JsonObject();

				for (Entry<Object, Object> entry : info.getAttributes()
						.entrySet()) {
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					metainfoEntry.addProperty(key, value);
				}

				if (info.getParent() != null)
					metainfoEntry.addProperty("parent", info.getParent());

				array.add(metainfoEntry);
			}

			return array;
		}
	}

}
