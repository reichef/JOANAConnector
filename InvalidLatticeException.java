/**
 * This file is part of the Joana IFC project. It is developed at the
 * Programming Paradigms Group of the Karlsruhe Institute of Technology.
 *
 * For further details on licensing please read the information at
 * http://joana.ipd.kit.edu or contact the authors.
 */
package edu.kit.joana.component.connector;

public class InvalidLatticeException extends RuntimeException {
	/**
	 * Constructor
	 *
	 * @param message a message describing the failed precondition.
	 */
	public InvalidLatticeException(String message) {
		super(message);
	}
}
