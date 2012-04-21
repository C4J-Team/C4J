package de.andrena.c4j.internal.compiler;

public class EmptyExp extends StandaloneExp {
	@Override
	public String getCode() {
		return "";
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
