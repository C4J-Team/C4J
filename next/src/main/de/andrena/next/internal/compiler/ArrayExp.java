package de.andrena.next.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import de.andrena.next.internal.util.ObjectConverter;

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

	public static ArrayExp forArgs(CtBehavior behavior) throws NotFoundException {
		return ArrayExp.forArgs(behavior, 1);
	}

	public static ArrayExp forArgs(CtBehavior behavior, int startIndex) throws NotFoundException {
		List<NestedExp> args = new ArrayList<NestedExp>();
		for (int i = 0; i < behavior.getParameterTypes().length; i++) {
			args.add(new StaticCallExp(ObjectConverter.toObject, NestedExp.arg(i + startIndex)));
		}
		return new ArrayExp(Object.class, args);
	}

	@Override
	protected String getCode() {
		return code;
	}

}
