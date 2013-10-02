package de.vksi.c4j.internal.transformer.util;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredConstructor;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredMethod;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.isBeforeInvariantHelperMethod;
import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.isClassInvariant;
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

import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;
import de.vksi.c4j.internal.contracts.InvolvedTypeInspector;
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
		if (isClassInvariant(contractBehavior) || isBeforeInvariantHelperMethod(contractBehavior)) {
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
		CtMethod affectedMethod = getAffectedMethodFromExtendedClasses(contractBehavior, affectedClass);
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
		return getAffectedMethodAsDelegate(affectedClass, contractBehavior, affectedMethod);
	}

	private CtMethod getAffectedMethodAsDelegate(CtClass affectedClass, CtBehavior contractBehavior,
			CtMethod affectedMethod) throws CannotCompileException {
		if (Modifier.isFinal(affectedMethod.getModifiers())) {
			LOGGER.warn("could not find method " + contractBehavior.getName() + " in affected class "
					+ affectedClass.getName() + " for contract class " + contractBehavior.getDeclaringClass().getName()
					+ " and cannot insert a delegate, as the overridden method is final");
			return null;
		}
		LOGGER.warn("could not find method " + contractBehavior.getName() + " in affected class "
				+ affectedClass.getName() + " for contract class " + contractBehavior.getDeclaringClass().getName()
				+ " - inserting an empty method");
		return insertDelegateMethod(affectedClass, affectedMethod);
	}

	private CtMethod insertDelegateMethod(CtClass affectedClass, CtMethod affectedMethod) throws CannotCompileException {
		CtMethod delegateMethod = CtNewMethod.delegator(affectedMethod, affectedClass);
		delegateMethod.setModifiers(Modifier.clear(delegateMethod.getModifiers(), Modifier.NATIVE));
		delegateMethod.setModifiers(Modifier.clear(delegateMethod.getModifiers(), Modifier.ABSTRACT));
		affectedClass.addMethod(delegateMethod);
		return delegateMethod;
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
		if (!affectedClass.equals(contractInfo.getTargetClass())) {
			return null;
		}
		CtConstructor affectedConstructor = getDeclaredConstructor(affectedClass, ClassAnalyzer
				.getConstructorParameterTypes(affectedClass, contractBehavior));
		if (affectedConstructor == null) {
			// TODO: error
			LOGGER.warn("could not find a matching constructor in affected class " + affectedClass.getName()
					+ " for constructor " + contractBehavior.getLongName());
			return null;
		}
		return affectedConstructor;
	}
}
