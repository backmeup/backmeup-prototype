package org.backmeup.model.serializer;

import java.lang.reflect.Type;
import java.util.Date;

import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.JobProtocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
  
  // We have to manually prevent the recursion between protocol <-> user <-> job
  private static class JobProtocolSerializer implements com.google.gson.JsonSerializer<JobProtocol>, JsonDeserializer<JobProtocol> {
    @Override
    public JobProtocol deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      // as we don't need the object, we simply return an emtpy element
      return new JobProtocol();
    }

    @Override
    public JsonElement serialize(JobProtocol src, Type typeOfSrc,
        JsonSerializationContext context) {
      // as we don't need the object, we simply return an emtpy element
      return new JsonObject();
    }
  }
  
 //We have to manually prevent the recursion between protocol <-> user <-> job
 private static class ActionPropertySerializer implements com.google.gson.JsonSerializer<ActionProperty>, JsonDeserializer<ActionProperty> {
   @Override
   public ActionProperty deserialize(JsonElement json, Type typeOfT,
       JsonDeserializationContext context) throws JsonParseException {
     // as we don't need the object, we simply return an emtpy element     
     return new ActionProperty(json.getAsJsonObject().get("key").getAsString(), json.getAsJsonObject().get("value").getAsString());
   }

   @Override
   public JsonElement serialize(ActionProperty src, Type typeOfSrc,
       JsonSerializationContext context) {
     // as we don't need the object, we simply return an emtpy element
     JsonObject jo = new JsonObject();
     jo.addProperty("key", src.getKey());
     jo.addProperty("value", src.getValue());
     return jo;
   }
 }
  
  private static GsonBuilder builder;
  
  static {
    builder = new GsonBuilder();
    builder.registerTypeAdapter(Date.class, new DateSerializer());
    builder.registerTypeAdapter(JobProtocol.class, new JobProtocolSerializer());
    builder.registerTypeAdapter(ActionProperty.class, new ActionPropertySerializer());
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
