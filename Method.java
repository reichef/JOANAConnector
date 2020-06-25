package edu.kit.joana.component.connector;

import java.util.Objects;

/**
 * A method with a name and a class, a method without any addition refers to the method signature (all parameters), but not
 * to the return value
 */
public class Method extends ProgramPart {

  public final String concreteName;
  public final String className;
  public final String methodName;

  public Method(String className, String methodName) {
    this.className = className;
    this.methodName = methodName;
    this.concreteName = "";
  }
  
  public Method(String concreteName, String className, String methodName) {
	  	this.concreteName = concreteName;
	    this.className = className;
	    this.methodName = methodName;
	  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Method))
      return false;
    Method method = (Method) o;
    return Objects.equals(className, method.className) && Objects.equals(methodName, method.methodName);
  }

  @Override public int hashCode() {
    return toString().hashCode();
  }

  @Override public String toString() {
    return "Method{" + className + "." + methodName + "}";
  }

  @Override public Method getOwningMethod() {
    return this;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  public String getClassName() {
    return className;
  }

  public String getRealClassName(){
    if (concreteName.isEmpty()) {
      return className;
    }
    return concreteName;
  }

  /** Creates a new instance with a different class name */
  @Override
  public ProgramPart setClassName(String newClassName) {
    Method method = new Method(newClassName, newClassName, methodName);
    method.getLevel().ifPresent(method::setLevel);
    return method;
  }
}
