package edu.kit.joana.component.connector;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Call for analyze
 */
public class JoanaCall {

  public final String classPath;

  public final Flows knownFlows;

  public final List<Method> sources;

  public final List<Method> sinks;

  public JoanaCall(String classPath, Flows knownFlows, List<Method> sources, List<Method> sinks) {
    this.classPath = classPath;
    this.knownFlows = knownFlows;
    this.sources = sources;
    this.sinks = sinks;
  }

  public JoanaCall setClassPath(String newClassPath) {
    return new JoanaCall(newClassPath, knownFlows, sources, sinks);
  }

  public void store(Path path) {
    Util.store(path, this);
  }

  public static JoanaCall load(Path path) {
    return Util.load(path);
  }

  private void deleteFolder(Path folder) {
    try {
      Files.walk(folder).map(Path::toFile).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(File::delete);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void roundTrip(Consumer<JoanaCall> processor) {
    try {
      Path tmpFile = Files.createTempFile("", ".zip");
      Path tmpFolder = Files.createTempDirectory("");
      storeWithClassPath(tmpFile);
      processor.accept(loadZipFile(tmpFile, tmpFolder));
      deleteFolder(tmpFolder);
      Files.delete(tmpFile);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a zip file that contains the content of the classpath and this object (in file call.json)
   *
   * @param path path of the generated zip file
   */
  public void storeWithClassPath(Path path) {
    try {
      Files.createDirectories(path.toAbsolutePath().getParent());
      Map<String, String> env = new HashMap<>();
      env.put("create", "true");
      if (Files.exists(path)) {
        Files.delete(path);
      }
      try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + path.toAbsolutePath().toString()), env, null)) {
        for (String classPathPart : classPath.split(":")) {
          Path classPathPartPath = Paths.get(classPathPart).toAbsolutePath();
          if (Files.isDirectory(classPathPartPath)) {
            Files.walk(classPathPartPath).forEach(filePath -> {
              try {
                Path newPath = fs.getPath("/" + classPathPartPath.relativize(filePath).toString());
                if (Files.exists(newPath)) {
                  return;
                }
                if (Files.isDirectory(filePath)) {
                  Files.createDirectories(newPath);
                } else {
                  Files.copy(filePath, newPath);
                }
              } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
              }
            });
          } else {
            Files.copy(classPathPartPath, fs.getPath(classPathPartPath.getFileName().toString()));
          }
        }
        Path tmpPath = Files.createTempFile("", "");
        setClassPath(".").store(tmpPath);
        Files.copy(tmpPath, fs.getPath("/call.json"));
        Files.delete(tmpPath);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static JoanaCall loadZipFile(Path zip, Path resultPath) {
    try {
      if (!Files.exists(resultPath)) {
        Files.createDirectory(resultPath);
      }
      ZipFile zipFile = new ZipFile(zip.toFile());
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();
        Path newFile = resultPath.resolve(Paths.get(zipEntry.getName()));
        Files.createDirectories(newFile.getParent());
        Files.copy(zipFile.getInputStream(zipEntry), newFile);
      }
      JoanaCall loaded = load(resultPath.resolve("call.json"));
      if (!loaded.classPath.equals(".")) {
        throw new RuntimeException();
      }
      return loaded.setClassPath(resultPath.toAbsolutePath().toString());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Loads a zip file into a temporary folder and processes it with the passed method
   */
  public static void loadZipFile(Path path, Consumer<JoanaCall> processor) {
    try {
      Path tmpFolder = Files.createTempDirectory("");
      processor.accept(loadZipFile(path, tmpFolder));
      Files.delete(tmpFolder);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
