package de.andrena.c4j.internal.compiler;

public abstract class Exp {
	protected abstract String getCode();

	@Override
	public String toString() {
		return getCode();
	}

}
