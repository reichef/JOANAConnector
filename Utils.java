package edu.kit.joana.component.connector;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class Utils {

  public static <T> T load(Path path) {
    try {
      return fromJson(String.join("\n", Files.readAllLines(path)));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJson(String json) {
    return (T) JsonReader.jsonToJava(json);
  }

  public static <T> String toJson(T obj){
    return JsonWriter.objectToJson(obj, Collections.singletonMap(JsonWriter.PRETTY_PRINT, true));
  }

  public static <T> void store(Path path, T obj) {
    try {
      Files.write(path, Collections.singleton(toJson(obj)));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
