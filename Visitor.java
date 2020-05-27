package edu.kit.joana.component.connector;

public interface Visitor<T> {

  T visit(Method method);

  T visit(MethodParameter parameter);

  T visit(MethodReturn methodReturn);
}
