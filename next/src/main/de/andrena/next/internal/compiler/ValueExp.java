package de.andrena.next.internal.compiler;

import javassist.CtClass;

public class ValueExp extends NestedExp {
	private String code;

	public ValueExp(Class<?> value) {
		code = getValueAsString(value);
	}

	public ValueExp(CtClass value) {
		code = getValueAsString(value);
	}

	public ValueExp(String value) {
		code = getValueAsString(value);
	}

	public ValueExp(int value) {
		code = Integer.toString(value);
	}

	public ValueExp(Enum<?> value) {
		code = getValueAsString(value);
	}

	public ValueExp(Object value, Class<?> targetClass) {
		code = getValueAsString(value, targetClass);
	}

	private String getValueAsString(Object value, Class<?> targetClass) {
		if (value instanceof Class) {
			return getValueAsString((Class<?>) value);
		}
		if (value instanceof CtClass) {
			return getValueAsString((CtClass) value);
		}
		if (value instanceof String) {
			return getValueAsString((String) value);
		}
		if (value instanceof Enum) {
			return getValueAsString((Enum<?>) value);
		}
		if (value instanceof Object[]) {
			return getValueAsString((Object[]) value, targetClass);
		}
		if (targetClass.isPrimitive()) {
			return "(" + targetClass.getName() + ") " + value.toString();
		}
		return value.toString();
	}

	private String getValueAsString(Class<?> value) {
		return value.getName() + ".class";
	}

	private String getValueAsString(CtClass value) {
		return value.getName() + ".class";
	}

	private String getValueAsString(String value) {
		return '"' + value + '"';
	}

	private String getValueAsString(Enum<?> value) {
		return value.getClass().getName() + "." + value.name();
	}

	private String getValueAsString(Object[] values, Class<?> targetClass) {
		if (values.length == 0) {
			return "new " + targetClass.getComponentType().getCanonicalName() + "[0]";
		}
		StringBuilder result = new StringBuilder("new " + targetClass.getComponentType().getCanonicalName() + "[] { ");
		boolean first = true;
		for (Object value : values) {
			if (!first) {
				result.append(", ");
			}
			result.append(getValueAsString(value, targetClass.getComponentType()));
			first = false;
		}
		result.append(" }");
		return result.toString();
	}

	@Override
	protected String getCode() {
		return code;
	}

}
