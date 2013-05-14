package de.vksi.c4j.internal.contracts;

import javassist.CtMethod;

public class ContractMethod {
	private CtMethod method;
	private boolean hasPreConditionOrDependencies;
	private boolean hasPostCondition;
	private boolean containsUnchanged;

	public ContractMethod(CtMethod method, boolean hasPreConditionOrDependencies, boolean hasPostCondition,
			boolean containsUnchanged) {
		this.method = method;
		this.hasPreConditionOrDependencies = hasPreConditionOrDependencies;
		this.hasPostCondition = hasPostCondition;
		this.containsUnchanged = containsUnchanged;
	}

	public CtMethod getMethod() {
		return method;
	}

	public boolean hasPreConditionOrDependencies() {
		return hasPreConditionOrDependencies;
	}

	public boolean hasPostCondition() {
		return hasPostCondition;
	}

	public boolean containsUnchanged() {
		return containsUnchanged;
	}

}