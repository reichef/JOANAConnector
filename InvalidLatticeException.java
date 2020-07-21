package edu.kit.joana.component.connector;

public class InvalidLatticeException extends RuntimeException {
	/**
	 * Constructor
	 *
	 * @param message
	 *            a message describing the failed precondition.
	 */
	public InvalidLatticeException(String message) {
		super(message);
	}
}
