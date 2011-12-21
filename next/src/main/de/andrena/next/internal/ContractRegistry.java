package de.andrena.next.internal;

import java.util.HashMap;
import java.util.Map;

import javassist.CtClass;

public class ContractRegistry {
	private Map<CtClass, ContractInfo> contractMap = new HashMap<CtClass, ContractInfo>();

	public void registerContract(ContractInfo contractInfo) {
		contractMap.put(contractInfo.getContractClass(), contractInfo);
	}

	public ContractInfo getContract(CtClass contractClass) {
		return contractMap.get(contractClass);
	}

	public boolean isContractClass(CtClass clazz) {
		return contractMap.containsKey(clazz);
	}
}
