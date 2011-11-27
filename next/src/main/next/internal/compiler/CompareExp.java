package next.internal.compiler;

public class CompareExp extends NestedExp {
	private String code;

	public CompareExp(NestedExp exp) {
		this(exp.getCode());
	}

	private CompareExp(String code) {
		this.code = code;
	}

	public CompareExp eq(NestedExp exp) {
		return new CompareExp("(" + code + " == " + exp.getCode() + ")");
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

	@Override
	protected String getCode() {
		return code;
	}
}
