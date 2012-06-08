package de.andrena.c4j;

/**
 * Thrown if there was an exception during execution of a contract.
 */
public class ContractError extends Error {
	private static final long serialVersionUID = 2863771757331138670L;

	public ContractError() {
		super();
	}

	public ContractError(String message, Throwable cause) {
		super(message, cause);
	}

	public ContractError(String message) {
		super(message);
	}

	public ContractError(Throwable cause) {
		super(cause);
	}

}
