package edu.kit.joana.component.connector;

import java.nio.file.Path;

public class JoanaCallReturn {

  public final Flows flows;

  public JoanaCallReturn(Flows flows) {
    this.flows = flows;
  }

  void store(Path path) {
    Util.store(path, this);
  }

  static JoanaCallReturn load(Path path){
    return Util.load(path);
  }
}
