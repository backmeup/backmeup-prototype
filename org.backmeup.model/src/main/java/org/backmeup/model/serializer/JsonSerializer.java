package org.backmeup.model.serializer;

import com.google.gson.Gson;

public class JsonSerializer {
  public static <T> String serialize(T entry) {
    Gson gson = new Gson();
    return gson.toJson(entry);
  }
  
  public static <T> T deserialize(String entry, Class<T> clazz) {
    Gson gson = new Gson();
    return gson.fromJson(entry, clazz);
  }
}
