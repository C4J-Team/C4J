package de.andrena.c4j.internal.compiler;

import java.util.List;

import javassist.CtMethod;

public class StaticCallExp extends NestedExp {
	private String code;

	public StaticCallExp(StaticCall call, NestedExp... params) {
		code = call.getCode() + getCodeForParams(params);
	}

	public StaticCallExp(StaticCall call, List<NestedExp> params) {
		this(call, params.toArray(new NestedExp[0]));
	}

	public StaticCallExp(CtMethod staticMethod, NestedExp... params) {
		code = staticMethod.getDeclaringClass().getName() + "#" + staticMethod.getName() + getCodeForParams(params);
	}

	public StaticCallExp(CtMethod staticMethod, List<NestedExp> params) {
		this(staticMethod, params.toArray(new NestedExp[0]));
	}

	@Override
	protected String getCode() {
		return code;
	}

}
