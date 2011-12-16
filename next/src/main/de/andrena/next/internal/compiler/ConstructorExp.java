package de.andrena.next.internal.compiler;

import javassist.CtClass;

public class ConstructorExp extends NestedExp {
	private String code;

	public ConstructorExp(Class<?> clazz, NestedExp... params) {
		code = "\n" + "new " + clazz.getName() + getCodeForParams(params);
	}

	public ConstructorExp(CtClass clazz, NestedExp... params) {
		code = "\n" + "new " + clazz.getName() + getCodeForParams(params);
	}

	public ConstructorExp(String className, NestedExp... params) {
		code = "\n" + "new " + className + getCodeForParams(params);
	}

	@Override
	protected String getCode() {
		return code;
	}
}
