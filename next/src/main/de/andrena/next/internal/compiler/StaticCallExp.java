package de.andrena.next.internal.compiler;

public class StaticCallExp extends NestedExp {
	private String code;

	public StaticCallExp(StaticCall call, NestedExp... params) {
		code = call.getCode();
		code += getCodeForParams(params);
	}

	@Override
	protected String getCode() {
		return code;
	}

}
