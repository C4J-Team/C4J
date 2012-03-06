package de.andrena.c4j.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CtClass;
import de.andrena.c4j.internal.compiler.NestedExp;

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
		private Map<String, List<NestedExp>> unchangeables = new HashMap<String, List<NestedExp>>();

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

		public Set<CtClass> getInnerContractClasses() {
			return Collections.unmodifiableSet(innerContractClasses);
		}

		public Map<String, List<NestedExp>> getUnchangeables() {
			return unchangeables;
		}

		public Set<CtClass> getAllContractClasses() {
			Set<CtClass> allContractClasses = new HashSet<CtClass>(innerContractClasses);
			allContractClasses.add(contractClass);
			return Collections.unmodifiableSet(allContractClasses);
		}

	}
}
