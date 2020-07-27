package edu.kit.joana.component.connector;

import java.util.Objects;
import java.util.Optional;

public abstract class ProgramPart {

  private String level = "";

  public Optional<String> getLevel() {
    return level.isEmpty() ? Optional.empty() : Optional.of(level);
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public abstract Method getOwningMethod();

  public abstract <T> T accept(Visitor<T> visitor);

  public abstract ProgramPart setClassName(String newClassName);

  public String getRealClassName() {
    return getOwningMethod().getRealClassName();
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ProgramPart))
      return false;
    ProgramPart that = (ProgramPart) o;
    return Objects.equals(getOwningMethod(), that.getOwningMethod());
  }

  @Override public int hashCode() {
    return Objects.hash(getOwningMethod());
  }
}
