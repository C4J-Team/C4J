package de.andrena.c4j.internal.compiler;

public class AssignmentExp extends NestedExp {

	private String code;

	public AssignmentExp(NestedExp left, NestedExp right) {
		code = left.getCode() + " = " + right.getCode();
	}

	@Override
	protected String getCode() {
		return code;
	}

}
