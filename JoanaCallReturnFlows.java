package edu.kit.joana.component.connector;

public class JoanaCallReturnFlows extends JoanaCallReturn {

  public final Flows flows;

  public JoanaCallReturnFlows(Flows flows) {
    this.flows = flows;
  }

  @Override
  public String toString() {
    return flows.toString();
  }

  @Override boolean isError() {
    return false;
  }

  <T> T accept(JoanaCallReturnVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
