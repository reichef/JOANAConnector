/**
 * This file is part of the Joana IFC project. It is developed at the
 * Programming Paradigms Group of the Karlsruhe Institute of Technology.
 *
 * For further details on licensing please read the information at
 * http://joana.ipd.kit.edu or contact the authors.
 */
package edu.kit.joana.component.connector;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * The implementations compare lattice arguments l using l.hashCode, and if-cascaded.
 * We would use String switch here, if we had any confidence that javac wouldn't generate calls to String.equals
 * (using perfect hashing). See, e.g.,
 *   https://blogs.oracle.com/darcy/entry/project_coin_string_switch_break
 *   http://javarevisited.blogspot.de/2014/05/how-string-in-switch-works-in-java-7.html
 * 
 * We might have decided to compare using == and require clients only use String constants {@link LowHighLattice#LOW} 
 * and {@link LowHighLattice#HIGH}, but this might bee too unexpected and cause subtle bugs. 
 * 
 * @author Martin Hecker <martin.hecker@kit.edu>
 */
public class LowHighLattice extends Lattice {
	public final static LowHighLattice INSTANCE = new LowHighLattice();
	
	private LowHighLattice() {
	}
	
	private final static Collection<String> elements = new Collection<String>() {

		@Override
		public int size() {	return 2; }

		@Override
		public boolean isEmpty() { return false; }

		@Override
		public boolean contains(Object o) {
			if (! (o instanceof String)) return false;
			String s = (String) o;
			return (LOW.equals(s) || HIGH.equals(s));
		}

		@Override
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				int  at = 0;
				@Override
				public boolean hasNext() {
					return at < 2;
				}

				@Override
				public String next() {
					return (at++ == 0 ? LOW : HIGH);
				}
			};
		}

		@Override
		public Object[] toArray() {
			return new String[] { LOW, HIGH };
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] a) {
			// String is final, so bug of, type system!!
			if (a.length >= 2) { 
				a[0] = (T) LOW;
				a[1] = (T) HIGH;
				return a;
			} else {
				return (T[]) new String[] { LOW, HIGH };
			}
		}

		@Override
		public boolean add(String e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return c.stream().allMatch( x -> x.equals(LOW) || x.equals(HIGH));
		}

		@Override
		public boolean addAll(Collection<? extends String> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		
	}; 

	/* (non-Javadoc)
	 * @see edu.kit.joana.ifc.sdg.lattice.IStaticLattice#greatestLowerBound(java.lang.Object, java.lang.Object)
	 */
	@Override
	public String greatestLowerBound(String s, String t) throws NotInLatticeException {
		assert ((LOW.equals(s) || HIGH.equals(s)) &&
		        (LOW.equals(t) || HIGH.equals(t)));
		if (s.hashCode() == LOW.hashCode()) {
			return LOW;
		} else {
			return t;
		}
	}

	/* (non-Javadoc)
	 * @see edu.kit.joana.ifc.sdg.lattice.IStaticLattice#leastUpperBound(java.lang.Object, java.lang.Object)
	 */
	@Override
	public String leastUpperBound(String s, String t) throws NotInLatticeException {
		assert ((LOW.equals(s) || HIGH.equals(s)) &&
		        (LOW.equals(t) || HIGH.equals(t)));
		if (s.hashCode() == HIGH.hashCode()) {
			return HIGH;
		} else {
			return t;
		}
	}

	/* (non-Javadoc)
	 * @see edu.kit.joana.ifc.sdg.lattice.IStaticLattice#getTop()
	 */
	@Override
	public String getTop() throws InvalidLatticeException {
		return HIGH;
	}

	/* (non-Javadoc)
	 * @see edu.kit.joana.ifc.sdg.lattice.IStaticLattice#getBottom()
	 */
	@Override
	public String getBottom() throws InvalidLatticeException {
		return LOW;
	}

	/* (non-Javadoc)
	 * @see edu.kit.joana.ifc.sdg.lattice.IStaticLattice#getElements()
	 */
	@Override
	public Collection<String> getElements() {
		return elements;
	}
	
	
}
