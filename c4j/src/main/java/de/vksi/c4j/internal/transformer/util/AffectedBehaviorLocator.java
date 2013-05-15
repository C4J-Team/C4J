package de.vksi.c4j.internal.transformer.util;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.constructorHasAdditionalParameter;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredConstructor;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredMethod;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.BEFORE_INVARIANT_METHOD_SUFFIX;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.isContractClassInitializer;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.isContractConstructor;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.InitializeContract;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;
import de.vksi.c4j.internal.types.ListOrderedSet;

public class AffectedBehaviorLocator {
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();
	private static final Logger LOGGER = Logger.getLogger(AffectedBehaviorLocator.class);

	public CtMethod getContractMethod(ContractInfo contract, CtMethod affectedMethod) throws NotFoundException {
		return getDeclaredMethod(contract.getContractClass(), affectedMethod.getName(), affectedMethod
				.getParameterTypes());
	}

	public CtBehavior getAffectedBehavior(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException, CannotCompileException {
		if (contractBehavior.hasAnnotation(ClassInvariant.class)) {
			return null;
		}
		if (contractBehavior.hasAnnotation(InitializeContract.class)) {
			return null;
		}
		if (contractBehavior.getName().endsWith(BEFORE_INVARIANT_METHOD_SUFFIX)) {
			return null;
		}
		if (isContractConstructor(contractBehavior)) {
			return getAffectedConstructor(contractInfo, affectedClass, contractBehavior);
		}
		if (isContractClassInitializer(contractBehavior)) {
			return affectedClass.getClassInitializer();
		}
		if (contractBehavior instanceof CtMethod) {
			return getAffectedMethod(contractInfo, affectedClass, contractBehavior);
		}
		throw new NotFoundException("contractBehavior " + contractBehavior.getLongName()
				+ " is neither constructor nor method");
	}

	CtMethod getAffectedMethod(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException, CannotCompileException {
		CtClass currentClass = affectedClass;
		CtMethod affectedMethod = getAffectedMethodFromExtendedClasses(contractBehavior, currentClass);
		if (affectedMethod == null) {
			LOGGER.warn("could not find a matching method in affected class " + affectedClass.getName()
					+ " for method '" + contractBehavior.getName() + "' in contract class "
					+ contractInfo.getContractClass().getName());
			return null;
		}
		if (affectedMethod.getDeclaringClass().equals(affectedClass)) {
			return affectedMethod;
		}
		if (hasContract(affectedMethod.getDeclaringClass(), contractInfo)) {
			return null;
		}
		if (Modifier.isFinal(affectedMethod.getModifiers())) {
			LOGGER.warn("could not find method " + contractBehavior.getName() + " in affected class "
					+ affectedClass.getName() + " for contract class " + contractInfo.getContractClass().getName()
					+ " and cannot insert a delegate, as the overridden method is final");
			return null;
		}
		LOGGER.warn("could not find method " + contractBehavior.getName() + " in affected class "
				+ affectedClass.getName() + " for contract class " + contractInfo.getContractClass().getName()
				+ " - inserting an empty method");
		affectedMethod = CtNewMethod.delegator(affectedMethod, affectedClass);
		affectedMethod.setModifiers(Modifier.clear(affectedMethod.getModifiers(), Modifier.NATIVE));
		affectedMethod.setModifiers(Modifier.clear(affectedMethod.getModifiers(), Modifier.ABSTRACT));
		affectedClass.addMethod(affectedMethod);
		return affectedMethod;
	}

	private CtMethod getAffectedMethodFromExtendedClasses(CtBehavior contractBehavior, CtClass currentClass)
			throws NotFoundException {
		while (currentClass != null) {
			CtMethod method = getDeclaredMethod(currentClass, contractBehavior.getName(), contractBehavior
					.getParameterTypes());
			if (method != null) {
				return method;
			}
			currentClass = currentClass.getSuperclass();
		}
		return null;
	}

	private boolean hasContract(CtClass clazz, ContractInfo contractInfo) throws NotFoundException {
		ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(clazz);
		ListOrderedSet<ContractInfo> contracts = ContractRegistry.INSTANCE.getContractsForTypes(involvedTypes, clazz);
		return contracts.contains(contractInfo);
	}

	CtConstructor getAffectedConstructor(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException {
		if (contractInfo.getTargetClass().isInterface()) {
			return null;
		}
		CtConstructor affectedConstructor = getDeclaredConstructor(affectedClass, getConstructorParameterTypes(
				affectedClass, contractBehavior));
		if (affectedConstructor == null) {
			LOGGER.warn("could not find a matching constructor in affected class " + affectedClass.getName()
					+ " for constructor " + contractBehavior.getLongName());
			return null;
		}
		if (contractBehavior instanceof CtMethod) {
			return affectedConstructor;
		}
		if (getDeclaredMethod(contractInfo.getContractClass(), ContractClassMemberHelper.CONSTRUCTOR_REPLACEMENT_NAME,
				contractBehavior.getParameterTypes()) != null) {
			return null;
		}
		return affectedConstructor;
	}

	private CtClass[] getConstructorParameterTypes(CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException {
		CtClass[] parameterTypes = contractBehavior.getParameterTypes();
		if (constructorHasAdditionalParameter(affectedClass)) {
			CtClass[] initialParameterTypes = parameterTypes;
			parameterTypes = new CtClass[parameterTypes.length + 1];
			parameterTypes[0] = affectedClass.getDeclaringClass();
			for (int i = 0; i < initialParameterTypes.length; i++) {
				parameterTypes[i + 1] = initialParameterTypes[i];
			}
		}
		return parameterTypes;
	}
}
