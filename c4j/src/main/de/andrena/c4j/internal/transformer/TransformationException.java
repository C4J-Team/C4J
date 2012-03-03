package de.andrena.c4j.internal.transformer;

public class TransformationException extends RuntimeException {

	private static final long serialVersionUID = 3700006949340498687L;

	public TransformationException() {
		super();
	}

	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransformationException(String message) {
		super(message);
	}

	public TransformationException(Throwable cause) {
		super(cause);
	}

}
