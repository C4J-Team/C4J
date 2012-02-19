package de.andrena.next.internal;

public class ContractException extends RuntimeException {
	private static final long serialVersionUID = 2863771757331138670L;

	public ContractException() {
		super();
	}

	public ContractException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContractException(String message) {
		super(message);
	}

	public ContractException(Throwable cause) {
		super(cause);
	}

}
