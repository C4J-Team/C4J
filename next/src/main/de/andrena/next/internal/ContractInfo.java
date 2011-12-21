package de.andrena.next.internal;

import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;

public class ContractInfo {
	private CtClass targetClass;
	private CtClass contractClass;
	private Set<CtClass> innerContractClasses = new HashSet<CtClass>();

	public ContractInfo(CtClass targetClass, CtClass contractClass) {
		this.targetClass = targetClass;
		this.contractClass = contractClass;
	}

	public void addInnerContractClass(CtClass innerContractClass) {
		innerContractClasses.add(innerContractClass);
	}

	public CtClass getTargetClass() {
		return targetClass;
	}

	public CtClass getContractClass() {
		return contractClass;
	}

	public Set<CtClass> getInnerContractClasses() {
		return innerContractClasses;
	}

}
