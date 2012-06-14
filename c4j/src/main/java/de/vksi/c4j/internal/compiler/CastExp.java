package de.vksi.c4j.internal.compiler;

import javassist.CtClass;

public class CastExp extends NestedExp {
	private String code;

	public CastExp(Class<?> castClass, NestedExp exp) {
		code = "((" + castClass.getCanonicalName() + ") " + exp.getCode() + ")";
	}

	public CastExp(CtClass castClass, NestedExp exp) {
		code = "((" + castClass.getName() + ") " + exp.getCode() + ")";
	}

	private CastExp(String code) {
		this.code = code;
	}

	public static CastExp forReturnType(NestedExp exp) {
		return new CastExp("(($r) " + exp.getCode() + ")");
	}

	@Override
	protected String getCode() {
		return code;
	}

}
