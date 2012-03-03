package de.andrena.c4j.internal.compiler;

public class StaticCall {
	private Class<?> callClass;
	private String callMethod;

	public StaticCall(Class<?> callClass, String callMethod) {
		super();
		this.callClass = callClass;
		this.callMethod = callMethod;
	}

	public Class<?> getCallClass() {
		return callClass;
	}

	public String getCallMethod() {
		return callMethod;
	}

	String getCode() {
		return callClass.getName() + "#" + callMethod;
	}

}
