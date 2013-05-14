package de.vksi.c4j.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CtClass;
import javassist.CtMethod;
import de.vksi.c4j.error.UsageError;

public class ContractRegistry {
	private Map<CtClass, ContractInfo> contractMap = new HashMap<CtClass, ContractInfo>();
	private Map<CtClass, ContractInfo> targetMap = new HashMap<CtClass, ContractInfo>();

	public ContractInfo registerContract(CtClass targetClass, CtClass contractClass) {
		if (isContractClass(contractClass)) {
			return getContractInfo(contractClass);
		}
		ContractInfo contractInfo = new ContractInfo(targetClass, contractClass);
		contractMap.put(contractClass, contractInfo);
		targetMap.put(targetClass, contractInfo);
		return contractInfo;
	}

	public ContractInfo getContractInfo(CtClass contractClass) {
		return contractMap.get(contractClass);
	}

	public boolean isContractClass(CtClass clazz) {
		return contractMap.containsKey(clazz);
	}

	public boolean hasRegisteredContract(CtClass targetClass) {
		return targetMap.containsKey(targetClass);
	}

	public ContractInfo getContractInfoForTargetClass(CtClass targetClass) {
		return targetMap.get(targetClass);
	}

	public class ContractInfo {
		private CtClass targetClass;
		private CtClass contractClass;
		private Set<CtClass> innerContractClasses = new HashSet<CtClass>();
		private Set<ContractMethod> methods = new HashSet<ContractMethod>();
		private List<UsageError> errors = new ArrayList<UsageError>();

		private ContractInfo(CtClass targetClass, CtClass contractClass) {
			this.targetClass = targetClass;
			this.contractClass = contractClass;
		}

		public void addInnerContractClass(CtClass innerContractClass) {
			innerContractClasses.add(innerContractClass);
			contractMap.put(innerContractClass, this);
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

	public static class ContractMethod {
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
}
