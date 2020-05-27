package edu.kit.joana.component.connector;

import java.nio.file.Path;
import java.util.List;

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

  void store(Path path) {
    Util.store(path, this);
  }

  static JoanaCall load(Path path){
    return Util.load(path);
  }
}
