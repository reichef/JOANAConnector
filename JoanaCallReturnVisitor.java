package edu.kit.joana.component.connector;

public interface JoanaCallReturnVisitor<T> {

  T visit(JoanaCallReturnError error);

  T visit(JoanaCallReturnFlows flows);

}
