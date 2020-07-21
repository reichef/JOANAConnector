package edu.kit.joana.component.connector;


import java.util.*;

public class Lattice {

  public static final String HIGH = "high";
  public static final String LOW = "low";

  public static final Lattice BASIC = LowHighLattice.INSTANCE;

  private List<String> elements = new ArrayList<String>();
  private Map<String, Collection<String>> lower = new HashMap<String, Collection<String>>();
  private Map<String, Collection<String>> greater = new HashMap<>();

  /**
   * Constructor
   */
  public Lattice() { }

  /**
   * Constructor
   *
   * @param elements
   *            the initial elements in the lattice
   */
  public Lattice(Collection<String> elements) {
    assert elements != null;
    for (String e : elements)
      if (!this.elements.contains(e))
        this.elements.add(e);
  }

  public void addElement(String element) {
    assert element != null;
    if (!elements.contains(element))
      elements.add(element);
  }

  public Collection<String> getElements() {
    return elements;
  }

  public void removeElement(String element) {
    assert element != null;
    if (!elements.contains(element))
      throw new NotInLatticeException("Element '" + element.toString() + "' is not in lattice");
    elements.remove(element);

    greater.remove(element);
    lower.remove(element);

    for (String e : elements) {
      Collection<String> greaterElements = greater.get(e);
      if (greaterElements != null) {
        greaterElements.remove(element);
        if (greaterElements.isEmpty())
          greater.remove(e);
      }
      Collection<String> lowerElements = lower.get(e);
      if (lowerElements != null) {
        lowerElements.remove(element);
        if (lowerElements.isEmpty())
          lower.remove(e);
      }
    }
  }

  public void setImmediatelyGreater(String element, String greater) {
    assert element != null;
    assert greater != null;
    if (!elements.contains(element))
      throw new NotInLatticeException("Element '" + element.toString() + "' is not in lattice");
    if (!elements.contains(greater))
      throw new NotInLatticeException("Element '" + greater.toString() + "' is not in lattice");

    Collection<String> greaterElements = this.greater.get(element);
    if (greaterElements == null)
      this.greater.put(element, greaterElements = new ArrayList<String>());
    if (!greaterElements.contains(greater))
      greaterElements.add(greater);

    Collection<String> lowerElements = this.lower.get(greater);
    if (lowerElements == null)
      this.lower.put(greater, lowerElements = new ArrayList<String>());
    if (!lowerElements.contains(element))
      lowerElements.add(element);
  }

  public void setImmediatelyLower(String element, String lower) {
    setImmediatelyGreater(lower, element);
  }

  public void unsetImmediatelyGreater(String element, String greater) {
    assert element != null;
    assert greater != null;
    if (!elements.contains(element))
      throw new NotInLatticeException("Element '" + element.toString() + "' is not in lattice");
    if (!elements.contains(greater))
      throw new NotInLatticeException("Element '" + greater.toString() + "' is not in lattice");

    Collection<String> greaterElements = this.greater.get(element);
    if (greaterElements != null) {
      greaterElements.remove(greater);
      if (greaterElements.isEmpty())
        this.greater.remove(element);
    }

    Collection<String> lowerElements = this.lower.get(greater);
    if (lowerElements != null) {
      lowerElements.remove(element);
      if (lowerElements.isEmpty())
        this.lower.remove(greater);
    }
  }

  public void unsetImmediatelyLower(String element, String lower) {
    unsetImmediatelyGreater(lower, element);
  }

  public Collection<String> getImmediatelyGreater(String element) {
    assert element != null;
    if (!elements.contains(element))
      throw new NotInLatticeException("Element '" + element.toString() + "' is not in lattice");
    Collection<String> ret = greater.get(element);
    if (ret == null)
      return new ArrayList<String>();
    return ret;
  }

  public Collection<String> getImmediatelyLower(String element) {
    assert element != null;
    if (!elements.contains(element))
      throw new NotInLatticeException("Element '" + element.toString() + "' is not in lattice");
    Collection<String> ret = lower.get(element);
    if (ret == null)
      return new ArrayList<String>();
    return ret;
  }

  public String greatestLowerBound(String s, String t) {
    assert s != null;
    assert t != null;
    assert elements.contains(s);
    assert elements.contains(t);
    Collection<String> greatestLowerBounds = greatestLowerBounds(s, t);
    if (greatestLowerBounds.size() != 1)
      throw new InvalidLatticeException("Graph is not a lattice");
    return greatestLowerBounds.iterator().next();
  }

  public String leastUpperBound(String s, String t) {
    assert s != null;
    assert t != null;
    assert elements.contains(s);
    assert elements.contains(t);
    Collection<String> leastUpperBounds = leastUpperBounds(s, t);
    if (leastUpperBounds.size() != 1)
      throw new InvalidLatticeException("Graph is not a lattice");
    return leastUpperBounds.iterator().next();
  }

  /**
   * Returns the unique bottom element of the lattice.
   *
   * @reutrn the unique bottom element of the lattice.
   * @throws InvalidLatticeException
   *             if the graph does not have a unique bottom element.
   */
  public String getBottom() {
    Collection<String> bottoms = this.findBottomElements(getElements());
    if (bottoms.size() != 1)
      throw new InvalidLatticeException("No unique bottom element found");
    return bottoms.iterator().next();
  }

  /**
   * Returns the unique top element of the lattice.
   *
   * @return the unique top element of the lattice.
   * @throws InvalidLatticeException
   *             if the graph does not have a unique top element.
   */
  public String getTop() {
    Collection<String> tops = this.findTopElements(getElements());
    if (tops.size() != 1)
      throw new InvalidLatticeException("No unique top element found");
    return tops.iterator().next();
  }

  /**
   * Transitive greater elements of a given lattice element.
   *
   * @param s
   *            the element for which to collect all greater elements.
   *
   * @return all transitive greater elements of <code>s</code>
   */
  public Collection<String> collectAllGreaterElements(String s) {
    Collection<String> greaterElements = new HashSet<String>();
    greaterElements.add(s);
    boolean changed = false;
    do {
      changed = false;
      Collection<String> toAdd = new ArrayList<String>();
      for (String e : greaterElements)
        for (String p : getImmediatelyGreater(e))
          if (!greaterElements.contains(p)) {
            toAdd.add(p);
            changed = true;
          }
      greaterElements.addAll(toAdd);
    } while (changed);
    return greaterElements;
  }

  /**
   * Transitive lower elements of a given lattice element.
   *
   * @param s
   *            the element for which to collect all lower elements.
   *
   * @return all transitive lower elements of <code>s</code>
   */
  public Collection<String> collectAllLowerElements(String s) {
    Collection<String> lowerElements = new HashSet<String>();
    lowerElements.add(s);
    boolean changed = false;
    do {
      changed = false;
      Collection<String> toAdd = new ArrayList<String>();
      for (String e : lowerElements)
        for (String p : getImmediatelyLower(e))
          if (!lowerElements.contains(p)) {
            toAdd.add(p);
            changed = true;
          }
      lowerElements.addAll(toAdd);
    } while (changed);
    return lowerElements;
  }
  
  /**
   * Finds all least upper bounds (lub) for two given elements.
   *
   * @param s
   *            the first parameter for the lub operation.
   * @param t
   *            the second parameter for the lub operation.
   * @return all least upper bounds of the elements <code>s</code> and
   *         <code>t</code>
   */
  public Collection<String> leastUpperBounds(String s, String t) {
    assert s != null;
    assert t != null;

    // GBs = {x in elements | x >= s}
    Collection<String> gbs = collectAllGreaterElements(s);

    // GBt = {x in elements | x >= t}
    Collection<String> gbt = collectAllGreaterElements(t);

    // CGB = GBs intersect GBt
    List<String> cgb = new ArrayList<String>();
    for (String a : gbs)
      if (gbt.contains(a))
        cgb.add(a);

    // return min(CLB)
    return min(cgb);
  }

  public Collection<String> min(Collection<String> elements) {
    Collection<String> ret = new ArrayList<String>();
    Elements: for (String e : elements) {
      for (String g : getImmediatelyLower(e)) {
        if (elements.contains(g))
          continue Elements;
      }
      ret.add(e);
    }
    return ret;
  }


  /**
   * Finds all greatest lower bounds (glb) for two given elements.
   *
   * @param s
   *            the first parameter for the glb operation.
   * @param t
   *            the second parameter for the glb operation.
   * @return all greatest lower bounds of the elements <code>s</code> and
   *         <code>t</code>
   */
  public Collection<String> greatestLowerBounds(String s, String t) {
    assert s != null;
    assert t != null;

    // LBs = {x in elements | x <= s}
    Collection<String> lbs = collectAllLowerElements(s);

    // LBt = {x in elements | x <= t}
    Collection<String> lbt = collectAllLowerElements(t);

    // CLB = LBs intersect LBt
    List<String> clb = new ArrayList<String>();
    for (String a : lbs)
      if (lbt.contains(a))
        clb.add(a);

    // return max(CLB)
    return max(clb);
  }

  public Collection<String> max(Collection<String> elements) {
    Collection<String> ret = new ArrayList<String>();
    Elements: for (String e : elements) {
      for (String g : getImmediatelyGreater(e)) {
        if (elements.contains(g))
          continue Elements;
      }
      ret.add(e);
    }
    return ret;
  }
  
  /**
   * Finds all elements that do not have a predecessor.
   *
   * @param inElements
   *            the elements of the graph.
   * @return all elements in <code>inElements</code> that do not have a
   *         predecessor.
   */
  public Collection<String> findTopElements(Collection<String> inElements) {
    Collection<String> tops = new ArrayList<String>();

    for (String e : inElements) {
      if (getImmediatelyGreater(e).size() == 0)
        tops.add(e);
    }
    return tops;
  }

  /**
   * Finds all elements that do not have a successor.
   *
   * @param inElements
   *            the elements of the graph.
   * @return all elements in <code>inElements</code> that do not have a
   *         successor.
   */
  public Collection<String> findBottomElements(Collection<String> inElements) {
    Collection<String> bottoms = new ArrayList<String>();

    for (String e : inElements) {
      if (getImmediatelyLower(e).size() == 0)
        bottoms.add(e);
    }
    return bottoms;
  }

  }
