package de.andrena.c4j.internal.compiler;

/**
 * Not supported by Javassist yet.
 */
public class AssertExp extends StandaloneExp {
	private StandaloneExp exp;

	public AssertExp(BooleanExp exp) {
		this.exp = CodeStandaloneExp.fromNested("assert " + exp.getCode());
	}

	@Override
	public String getCode() {
		return exp.getCode();
	}

}
