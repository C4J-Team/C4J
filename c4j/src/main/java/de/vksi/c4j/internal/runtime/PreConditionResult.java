package de.vksi.c4j.internal.runtime;

public class PreConditionResult {
	private boolean hasError = false;
	private boolean hasSuccess = false;
	private Throwable firstError;

	public boolean hasError() {
		return hasError;
	}

	public boolean hasSuccess() {
		return hasSuccess;
	}

	public Throwable getFirstError() {
		return firstError;
	}

	public void registerError(Throwable error) {
		hasError = true;
		if (firstError == null) {
			firstError = error;
		}
	}

	public void registerSuccess() {
		hasSuccess = true;
	}
}