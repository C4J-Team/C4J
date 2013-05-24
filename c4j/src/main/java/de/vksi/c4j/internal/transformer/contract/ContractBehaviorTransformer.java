package de.vksi.c4j.internal.transformer.contract;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.constructorHasAdditionalParameter;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredMethods;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getField;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getMethod;
import static de.vksi.c4j.internal.classfile.ClassAnalyzer.isSynthetic;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.classfile.BehaviorFilter;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper;

public class ContractBehaviorTransformer extends AbstractContractClassTransformer {
	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (contractClass.equals(contractInfo.getContractClass())) {
			if (!(contractInfo.getTargetClass().isInterface())) {
				checkConstructors(contractInfo, contractClass);
				removeSuperclass(contractClass);
				removeConstructorSuperCall(contractClass);
				checkMatchingStaticMethods(contractInfo);
			}
			makeClassAccessible(contractClass);
			makeAllBehaviorsAccessible(contractClass);
		}
	}

	private void removeConstructorSuperCall(CtClass contractClass) throws CannotCompileException, NotFoundException {
		CtConstructor constructor = contractClass.getDeclaredConstructors()[0];
		constructor.instrument(new ExprEditor() {
			@Override
			public void edit(ConstructorCall constructorCall) throws CannotCompileException {
				constructorCall.replace("super();");
			}
		});
	}

	private void makeClassAccessible(CtClass contractClass) {
		contractClass.setModifiers(Modifier.setPublic(contractClass.getModifiers()));
	}

	private void checkMatchingStaticMethods(ContractInfo contractInfo) {
		for (CtMethod contractMethod : getDeclaredMethods(contractInfo.getContractClass(), BehaviorFilter.STATIC,
				BehaviorFilter.VISIBLE)) {
			if (getMethod(contractInfo.getTargetClass(), contractMethod.getName(), contractMethod.getSignature()) == null
					&& !ContractClassMemberHelper.isContractClassInitializer(contractMethod)
					&& !isSynthetic(contractMethod)) {
				contractInfo.addError(new UsageError("Couldn't find matching target method for static contract method "
						+ contractMethod.getLongName() + "."));
			}
		}
	}

	private void makeAllBehaviorsAccessible(CtClass contractClass) {
		for (CtBehavior behavior : contractClass.getDeclaredBehaviors()) {
			behavior.setModifiers(Modifier.setPublic(behavior.getModifiers()));
		}
	}

	private void checkConstructors(ContractInfo contractInfo, CtClass contractClass) throws CannotCompileException,
			NotFoundException {
		if (constructorHasAdditionalParameter(contractClass)) {
			contractInfo.addError(new UsageError("Contract class " + contractClass.getName()
					+ " cannot be a nested, non-static class."));
			addNoArgConstructorToRaiseExceptionInStaticInitializer(contractClass);
			return;
		}
		if (!contractHasNoArgConstructor(contractClass)) {
			contractInfo.addError(new UsageError("Contract class " + contractClass.getName()
					+ " must have a non-arg constructor."));
			addNoArgConstructorToRaiseExceptionInStaticInitializer(contractClass);
		}
		if (contractClass.getDeclaredConstructors().length > 1) {
			contractInfo.addError(new UsageError("Contract class " + contractClass.getName()
					+ " may only have a single non-arg constructor."));
		}
	}

	private void addNoArgConstructorToRaiseExceptionInStaticInitializer(CtClass contractClass)
			throws CannotCompileException {
		contractClass.addConstructor(CtNewConstructor.defaultConstructor(contractClass));
	}

	private boolean contractHasNoArgConstructor(CtClass contractClass) throws NotFoundException {
		for (CtConstructor constructor : contractClass.getDeclaredConstructors()) {
			if (constructor.getParameterTypes().length == 0) {
				return true;
			}
		}
		return false;
	}

	private void removeSuperclass(CtClass contractClass) throws NotFoundException, CannotCompileException {
		if (contractClass.getSuperclass() != null) {
			CtClass oldSuperclass = contractClass.getSuperclass();
			contractClass.getClassFile().setSuperclass(null);
			addFieldsFromSuperclass(contractClass, oldSuperclass);
		}
	}

	private void addFieldsFromSuperclass(CtClass contractClass, CtClass superclass) throws CannotCompileException,
			NotFoundException {
		for (CtField superclassField : superclass.getFields()) {
			if (!hasField(contractClass, superclassField) && !Modifier.isStatic(superclassField.getModifiers())) {
				contractClass.addField(new CtField(superclassField, contractClass));
			}
		}
		if (superclass.getSuperclass() != null) {
			addFieldsFromSuperclass(contractClass, superclass.getSuperclass());
		}
	}

	private boolean hasField(CtClass contractClass, CtField superclassField) throws NotFoundException {
		return getField(contractClass, superclassField.getName()) != null;
	}
}
