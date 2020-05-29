package edu.kit.joana.component.connector;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class Util {

  public static <T> T load(Path path) {
    try {
      return (T) JsonReader.jsonToJava(String.join("\n", Files.readAllLines(path)));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static <T> void store(Path path, T obj) {
    try {
      Files.write(path, Collections.singleton(JsonWriter.objectToJson(obj)));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
