package edu.kit.joana.component.connector;

import java.util.Objects;

/**
 * A method with an associated parameters
 *
 * <b>equals, hashCode and toRegexp ignore the parameter number</b>
 */
public class MethodParameter extends ProgramPart {

  public final Method method;
  public final int parameter;

  public MethodParameter(Method method, int parameter) {
    this.method = method;
    this.parameter = parameter;
  }

  @Override public String toString() {
    return "MethodParameter{" + method + "->" + parameter + "}";
  }

  @Override public boolean equals(Object o) {
    System.out.println(this + " + " + o);
    if (this == o) {
      return true;
    }
    if (!(o instanceof MethodParameter)) {
      return false;
    }
    MethodParameter that = (MethodParameter) o;
    return parameter == that.parameter;
  }

  @Override public int hashCode() {
    System.out.println(Objects.hash(method, parameter) + ": " + toString());
    return Objects.hash(method, parameter);
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
    MethodParameter param = new MethodParameter((Method) method.setClassName(newClassName), parameter);
    param.getLevel().ifPresent(param::setLevel);
    return param;
  }
}
