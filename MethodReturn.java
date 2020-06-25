package edu.kit.joana.component.connector;

import java.util.Objects;

public class MethodReturn extends ProgramPart {

  public final Method method;

  public MethodReturn(Method method) {
    this.method = method;
  }

  @Override public String toString() {
    return "MethodReturn{" + method + "}";
  }

  @Override public Method getOwningMethod() {
    return method;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public ProgramPart setClassName(String newClassName) {
    MethodReturn ret = new MethodReturn((Method) method.setClassName(newClassName));
    ret.getLevel().ifPresent(ret::setLevel);
    return ret;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof MethodReturn))
      return false;
    MethodReturn that = (MethodReturn) o;
    return Objects.equals(method, that.method);
  }

  @Override public int hashCode() {
    return Objects.hash(method);
  }
}
