package edu.kit.joana.component.connector;

public class JoanaCallReturnError extends JoanaCallReturn {

  public final String errorMessage;

  public JoanaCallReturnError(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override public String toString() {
    return String.format("Error(%s)", errorMessage);
  }

  @Override boolean isError() {
    return true;
  }

  <T> T accept(JoanaCallReturnVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
