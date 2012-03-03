package de.andrena.c4j.internal.compiler;

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
