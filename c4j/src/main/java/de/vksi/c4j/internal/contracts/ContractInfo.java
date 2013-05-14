package de.vksi.c4j.internal.contracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CtClass;
import javassist.CtMethod;
import de.vksi.c4j.error.UsageError;

public class ContractInfo {
	private CtClass targetClass;
	private CtClass contractClass;
	private Set<CtClass> innerContractClasses = new HashSet<CtClass>();
	private Set<ContractMethod> methods = new HashSet<ContractMethod>();
	private List<UsageError> errors = new ArrayList<UsageError>();

	public ContractInfo(CtClass targetClass, CtClass contractClass) {
		this.targetClass = targetClass;
		this.contractClass = contractClass;
	}

	public CtClass getTargetClass() {
		return targetClass;
	}

	public CtClass getContractClass() {
		return contractClass;
	}

	public void addError(UsageError error) {
		errors.add(error);
	}

	public List<UsageError> getErrors() {
		return errors;
	}

	public Set<CtClass> getInnerContractClasses() {
		return Collections.unmodifiableSet(innerContractClasses);
	}

	public Set<CtClass> getAllContractClasses() {
		Set<CtClass> allContractClasses = new HashSet<CtClass>(innerContractClasses);
		allContractClasses.add(contractClass);
		return Collections.unmodifiableSet(allContractClasses);
	}

	public Set<CtMethod> getMethodsContainingUnchanged() {
		Set<CtMethod> methodsContainingUnchanged = new HashSet<CtMethod>();
		for (ContractMethod contractMethod : methods) {
			if (contractMethod.containsUnchanged()) {
				methodsContainingUnchanged.add(contractMethod.getMethod());
			}
		}
		return Collections.unmodifiableSet(methodsContainingUnchanged);
	}

	public void addMethod(CtMethod method, boolean hasPreConditionOrDependencies, boolean hasPostCondition,
			boolean containsUnchanged) {
		methods.add(new ContractMethod(method, hasPreConditionOrDependencies, hasPostCondition, containsUnchanged));
	}

	public Set<ContractMethod> getMethods() {
		return Collections.unmodifiableSet(methods);
	}
}