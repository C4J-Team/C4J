package de.vksi.c4j.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

public class ArrayExp extends NestedExp {

	private String code;

	public ArrayExp(Class<?> arrayClass, List<NestedExp> values) {
		this(arrayClass, values.toArray(new NestedExp[0]));
	}

	public ArrayExp(Class<?> arrayClass, NestedExp... values) {
		if (values.length == 0) {
			this.code = "new " + arrayClass.getName() + "[0]";
		} else {
			this.code = "new " + arrayClass.getName() + "[] { " + getCodeForValues(values) + " }";
		}
	}

	public static ArrayExp forParamTypes(CtBehavior behavior) throws NotFoundException {
		List<NestedExp> paramTypes = new ArrayList<NestedExp>();
		for (CtClass paramClass : behavior.getParameterTypes()) {
			paramTypes.add(new ValueExp(paramClass));
		}
		return new ArrayExp(Class.class, paramTypes);
	}

	@Override
	protected String getCode() {
		return code;
	}

}
