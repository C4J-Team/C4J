package de.vksi.c4j.internal.compiler;

public class CompareExp extends BooleanExp {
	private String code;

	public CompareExp(NestedExp exp) {
		this(exp.getCode());
	}

	private CompareExp(String code) {
		super(code);
		this.code = code;
	}

	public CompareExp eq(NestedExp exp) {
		return new CompareExp("(" + code + " == " + exp.getCode() + ")");
	}

	public CompareExp ne(NestedExp exp) {
		return new CompareExp("(" + code + " != " + exp.getCode() + ")");
	}

	public CompareExp gt(NestedExp exp) {
		return new CompareExp("(" + code + " > " + exp.getCode() + ")");
	}

	public CompareExp ge(NestedExp exp) {
		return new CompareExp("(" + code + " >= " + exp.getCode() + ")");
	}

	public CompareExp lt(NestedExp exp) {
		return new CompareExp("(" + code + " < " + exp.getCode() + ")");
	}

	public CompareExp le(NestedExp exp) {
		return new CompareExp("(" + code + " <= " + exp.getCode() + ")");
	}

	public CompareExp isEqual(NestedExp exp) {
		return new CompareExp(code + ".equals(" + exp.getCode() + ")");
	}

	public CompareExp isNotEqual(NestedExp exp) {
		return new CompareExp("!" + code + ".equals(" + exp.getCode() + ")");
	}

	@Override
	protected String getCode() {
		return code;
	}
}
