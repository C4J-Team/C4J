package de.vksi.c4j.internal.contracts;

import static de.vksi.c4j.internal.util.CollectionsHelper.arrayContains;

import java.util.HashMap;
import java.util.Map;

import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.configuration.XmlLocalConfiguration;
import de.vksi.c4j.internal.util.ListOrderedSet;

public class ContractRegistry {
	public static final ContractRegistry INSTANCE = new ContractRegistry();

	private static final Logger LOGGER = Logger.getLogger(ContractRegistry.class);

	final Map<CtClass, ContractInfo> contractMap = new HashMap<CtClass, ContractInfo>();
	private final Map<CtClass, ContractInfo> targetMap = new HashMap<CtClass, ContractInfo>();
	private final Map<XmlLocalConfiguration, Map<String, String>> externalContracts = new HashMap<XmlLocalConfiguration, Map<String, String>>();

	ContractRegistry() {
	}

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

	public CtClass getExternalContract(CtClass type, CtClass affectedClass) throws NotFoundException {
		Map<String, String> localExternalContracts = externalContracts.get(XmlConfigurationManager.INSTANCE
				.getConfiguration(affectedClass));
		if (localExternalContracts.containsKey(type.getName())) {
			return ClassFilePool.INSTANCE.getClass(localExternalContracts.get(type.getName()));
		}
		return null;
	}

	public ListOrderedSet<ContractInfo> getContractsForTypes(ListOrderedSet<CtClass> types, CtClass affectedClass)
			throws NotFoundException {
		ListOrderedSet<ContractInfo> contracts = new ListOrderedSet<ContractInfo>();
		for (CtClass type : types) {
			CtClass externalContract = getExternalContract(type, affectedClass);
			if (type.hasAnnotation(ContractReference.class) || externalContract != null) {
				if (hasRegisteredContract(type)) {
					contracts.add(getContractInfoForTargetClass(type));
				} else {
					CtClass contractClass = decideContractForType(type, externalContract);
					if (verifyContract(type, contractClass, affectedClass)) {
						contracts.add(registerContract(type, contractClass));
					}
				}
			}
		}
		return contracts;
	}

	private CtClass decideContractForType(CtClass type, CtClass externalContract) throws NotFoundException {
		if (type.hasAnnotation(ContractReference.class)) {
			return ClassFilePool.INSTANCE.getClassFromAnnotationValue(type, ContractReference.class, "value");
		}
		return externalContract;
	}

	private boolean verifyContract(CtClass targetClass, CtClass contractClass, CtClass affectedClass)
			throws NotFoundException {
		if (contractClass.hasAnnotation(Transformed.class)) {
			LOGGER.error("Ignoring contract class " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as it has been loaded before the target type was loaded.");
			return false;
		}
		if (contractClass.isInterface()) {
			LOGGER.error("Ignoring contract " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as the contract class is an interface.");
			return false;
		}
		if (contractClass.equals(targetClass)) {
			LOGGER.error("Ignoring contract " + contractClass.getSimpleName() + " defined on "
					+ targetClass.getSimpleName() + " as the contract class is the same as the target class.");
			return false;
		}
		if (contractClass.equals(affectedClass)) {
			LOGGER.error("Class " + contractClass.getSimpleName()
					+ " cannot be its own contract-class. Try explicitly marking " + contractClass.getSimpleName()
					+ " with @Contract.");
			return false;
		}
		warnContractNotInheritingFromTarget(targetClass, contractClass);
		return true;
	}

	private void warnContractNotInheritingFromTarget(CtClass targetClass, CtClass contractClass)
			throws NotFoundException {
		if (targetClass.isInterface()) {
			if (!arrayContains(contractClass.getInterfaces(), targetClass)) {
				logWarnContractNotInheritingFromTarget(targetClass, contractClass);
			}
		} else {
			if (!contractClass.getSuperclass().equals(targetClass) && !Modifier.isFinal(targetClass.getModifiers())) {
				logWarnContractNotInheritingFromTarget(targetClass, contractClass);
			}
		}
	}

	private void logWarnContractNotInheritingFromTarget(CtClass targetClass, CtClass contractClass) {
		LOGGER.warn("Contract type " + contractClass.getSimpleName()
				+ " does not inherit from its non-final target type " + targetClass.getSimpleName() + ".");
	}

	public void registerExternalContract(XmlLocalConfiguration xmlLocalConfiguration) throws Exception {
		externalContracts.put(xmlLocalConfiguration, new ContractPackageScanner(xmlLocalConfiguration
				.getContractScanPackages(), xmlLocalConfiguration.getClassLoader()).getExternalContracts());
	}
}
