package de.andrena.next.internal.compiler;


public class ThrowExp extends StandaloneExp {
	private StandaloneExp exp;

	public ThrowExp(NestedExp exp) {
		this.exp = CodeStandaloneExp.fromNested("throw " + exp.getCode());
	}

	@Override
	public String getCode() {
		return exp.getCode();
	}

}
