package edu.kit.joana.component.connector;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Found flows
 */
public class Flows implements Iterable<Map.Entry<ProgramPart, Set<ProgramPart>>> {

  private final Map<ProgramPart, Set<ProgramPart>> flows;

  public Flows() {
    this(new HashMap<>());
  }

  public Flows(Map<ProgramPart, Set<ProgramPart>> flows) {
    this.flows = flows;
  }

  public Map<ProgramPart, Set<ProgramPart>> flows() {
    return Collections.unmodifiableMap(flows);
  }

  public Set<ProgramPart> flows(ProgramPart part) {
    return flows.getOrDefault(part, Collections.emptySet());
  }

  public boolean isEmpty() {
    return flows.isEmpty();
  }

  /**
   * @return this - other
   */
  public Flows remove(Flows other) {
    Map<ProgramPart, Set<ProgramPart>> newFlows = new HashMap<>();
    for (Map.Entry<ProgramPart, Set<ProgramPart>> partSetEntry : flows.entrySet()) {
      if (other.flows.containsKey(partSetEntry.getKey())) {
        Set<ProgramPart> parts = new HashSet<>();
        for (ProgramPart part : partSetEntry.getValue()) {
          if (!other.flows.get(partSetEntry.getKey()).contains(part)) {
            parts.add(part);
          }
        }
        newFlows.put(partSetEntry.getKey(), parts);
      } else {
        newFlows.put(partSetEntry.getKey(), partSetEntry.getValue());
      }
    }
    return new Flows(newFlows);
  }

  @Override public String toString() {
    return flows.entrySet().stream().map(e -> String.format("%s -> %s", e.getKey(), e.getValue()))
        .collect(Collectors.joining("\n"));
  }

  public Flows add(ProgramPart source, ProgramPart sink) {
    flows.computeIfAbsent(source, s -> new HashSet<>()).add(sink);
    return this;
  }

  public Flows forMethod(Method method) {
    return forMethod(method, false);
  }

  /**
   * Collect all flows that start in the passed method
   *
   * @param method
   * @param onlyInnerMethodSinks only return sinks that belong to the same method
   * @return new flows instance
   */
  public Flows forMethod(Method method, boolean onlyInnerMethodSinks) {
    return filter(s -> s.getOwningMethod().equals(method),
        s -> !onlyInnerMethodSinks || s.getOwningMethod().equals(method));
  }

  public Flows onlyParameterSources() {
    return filterSources(s -> s instanceof MethodParameter);
  }

  public boolean contains(ProgramPart sink, ProgramPart source) {
    return flows(sink).contains(source);
  }

  public List<MethodParameter> getParamConnectedToReturn(Method method) {
    Flows parameterFlows = forMethod(method, true).onlyParameterSources();
    return parameterFlows.flows.keySet().stream().filter(p -> parameterFlows.contains(p, new MethodReturn(method)))
        .map(p -> (MethodParameter) p).collect(Collectors.toList());
  }

  public Flows filter(Predicate<ProgramPart> sourceFilter, Predicate<ProgramPart> sinkFilter) {
    return new Flows(flows.entrySet().stream().filter(e -> sourceFilter.test(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().filter(sinkFilter).collect(Collectors.toSet()))));
  }

  public Flows filterSources(Predicate<ProgramPart> sourceFilter) {
    return new Flows(flows.entrySet().stream().filter(e -> sourceFilter.test(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  public Flows filterSinks(Predicate<ProgramPart> sinkFilter) {
    return new Flows(flows.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().filter(sinkFilter).collect(Collectors.toSet()))));
  }

  @Override public Iterator<Map.Entry<ProgramPart, Set<ProgramPart>>> iterator() {
    return Collections.unmodifiableCollection(flows.entrySet()).iterator();
  }

  public int size() {
    return flows.values().stream().mapToInt(Set::size).sum();
  }
}
