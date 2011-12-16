package de.andrena.next.internal.compiler;

public abstract class Exp {
	protected abstract String getCode();

	@Override
	public String toString() {
		return getCode();
	}

}
