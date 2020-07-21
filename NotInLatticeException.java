package edu.kit.joana.component.connector;

public class NotInLatticeException extends RuntimeException {
	
	String description = "";

	/**
	 * Constructor
	 * @param description the description of the problem
	 */
	public NotInLatticeException(String description) {
		this.description = description;
	}

	public String toString() {
		return description;
	}
}
