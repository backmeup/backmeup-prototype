package org.backmeup.model.serializer;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class JsonSerializer {
  private static class DateSerializer implements com.google.gson.JsonSerializer<Date>, JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      long time = json.getAsLong();
      return new Date(time);
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc,
        JsonSerializationContext context) {      
      return new JsonPrimitive(src.getTime());
    }
  }
  
  private static GsonBuilder builder;
  
  static {
    builder = new GsonBuilder();
    builder.registerTypeAdapter(Date.class, new DateSerializer());
  }
  
  public static <T> String serialize(T entry) {
    Gson gson = builder.create();
    return gson.toJson(entry);
  }
  
  public static <T> T deserialize(String entry, Class<T> clazz) {
    Gson gson = builder.create();
    return gson.fromJson(entry, clazz);
  }
}
