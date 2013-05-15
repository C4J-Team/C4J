package de.vksi.c4j.internal.contracts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.Contract;
import de.vksi.c4j.Contract.InheritedType;
import de.vksi.c4j.internal.classfile.BackdoorAnnotationLoader;
import de.vksi.c4j.internal.configuration.C4JLocal.Configuration.ContractScanPackage;

public class ContractPackageScanner {
	private static final Logger LOGGER = Logger.getLogger(ContractPackageScanner.class);
	private final List<ContractScanPackage> contractScanPackages;
	private final ClassLoader classLoader;
	private final Map<String, String> externalContracts = new HashMap<String, String>();

	public ContractPackageScanner(List<ContractScanPackage> contractScanPackages, ClassLoader classLoader)
			throws Exception {
		this.contractScanPackages = contractScanPackages;
		this.classLoader = classLoader;
		scan();
	}

	public Map<String, String> getExternalContracts() {
		return externalContracts;
	}

	private void scan() throws Exception {
		for (ContractScanPackage contractScanPackage : contractScanPackages) {
			scanPackage(contractScanPackage);
		}
	}

	private void scanPackage(ContractScanPackage contractScanPackage) throws Exception {
		List<CtClass> classes = new ClasspathScanner(contractScanPackage.getValue(), contractScanPackage
				.isIncludeSubpackages(), classLoader).getAllClasses();
		for (CtClass clazz : classes) {
			handleClassFileInPackage(clazz);
		}
	}

	private void handleClassFileInPackage(CtClass clazz) throws Exception {
		if (clazz.hasAnnotation(Contract.class)) {
			handleContractClass(clazz);
		}
	}

	private void handleContractClass(CtClass loadedClass) {
		String targetFromAnnotation = new BackdoorAnnotationLoader(loadedClass).getClassValue(Contract.class,
				"forTarget");
		String contractClass = loadedClass.getName();
		if (targetFromAnnotation != null && !targetFromAnnotation.equals(InheritedType.class.getName())) {
			addExternalContract(targetFromAnnotation, contractClass);
			return;
		}
		handleContractClassInheritingTargetClass(loadedClass, contractClass);
	}

	private void handleContractClassInheritingTargetClass(CtClass loadedClass, String contractClass) {
		if (!loadedClass.getClassFile().getSuperclass().equals(Object.class.getName())) {
			addExternalContract(loadedClass.getClassFile().getSuperclass(), contractClass);
			return;
		}
		if (loadedClass.getClassFile().getInterfaces().length == 1) {
			addExternalContract(loadedClass.getClassFile().getInterfaces()[0], contractClass);
			return;
		}
		LOGGER.error("Contract "
				+ loadedClass.getName()
				+ " was found in ContractsDirectory, but could not be mapped to a target class. "
				+ "Extend from the target class or implement the target interface (while not implementing any other interfaces) "
				+ "or simply declare the target class within the @Contract annotation.");
	}

	private void addExternalContract(String targetClassName, String contractClassName) {
		externalContracts.put(targetClassName, contractClassName);
	}
}
