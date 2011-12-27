package de.andrena.next.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CtClass;

public class ContractRegistry {
	private Map<CtClass, ContractInfo> contractMap = new HashMap<CtClass, ContractInfo>();

	public ContractInfo registerContract(CtClass targetClass, CtClass contractClass) {
		ContractInfo contractInfo = new ContractInfo(targetClass, contractClass);
		contractMap.put(contractInfo.getContractClass(), contractInfo);
		return contractInfo;
	}

	public ContractInfo getContractInfo(CtClass contractClass) {
		return contractMap.get(contractClass);
	}

	public boolean isContractClass(CtClass clazz) {
		return contractMap.containsKey(clazz);
	}

	public class ContractInfo {
		private CtClass targetClass;
		private CtClass contractClass;
		private Set<CtClass> innerContractClasses = new HashSet<CtClass>();

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

		public Set<CtClass> getAllContractClasses() {
			Set<CtClass> allContractClasses = new HashSet<CtClass>(innerContractClasses);
			allContractClasses.add(contractClass);
			return Collections.unmodifiableSet(allContractClasses);
		}

	}
}
