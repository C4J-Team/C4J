package de.andrena.next.internal.transformer;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import de.andrena.next.ClassInvariant;
import de.andrena.next.internal.compiler.ArrayExp;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.TryExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.ObjectConverter;

public class BeforeAndAfterTriggerTransformer extends AffectedClassTransformerForSingleContract {

	@Override
	public void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception {
		for (CtBehavior contractBehavior : contractInfo.getContractClass().getDeclaredBehaviors()) {
			transform(contractInfo, affectedClass, contractBehavior);
		}
	}

	public void transform(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws Exception {
		CtBehavior affectedBehavior = getAffectedBehavior(contractInfo, affectedClass, contractBehavior);
		if (affectedBehavior == null) {
			return;
		}
		if (Modifier.isAbstract(affectedBehavior.getModifiers())) {
			return;
		}
		logger.info("transforming method " + affectedBehavior.getLongName() + ", triggered by "
				+ contractBehavior.getLongName());

		IfExp callPreCondition = getPreConditionCall(contractInfo, affectedClass, contractBehavior);
		IfExp callPostCondition = getPostConditionCall(contractInfo, affectedClass, contractBehavior, affectedBehavior);

		logger.info("preCondition: " + callPreCondition.getCode());
		logger.info("postCondition: " + callPostCondition.getCode());
		callPreCondition.insertBefore(affectedBehavior);
		callPostCondition.insertAfter(affectedBehavior);
	}

	private IfExp getPostConditionCall(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior,
			CtBehavior affectedBehavior) throws NotFoundException {
		TryExp callContractPost = new TryExp(getContractCallExp(contractInfo.getContractClass(), affectedClass,
				contractBehavior));
		callContractPost.addFinally(getAfterContractCall().append(getAfterContractMethodCall()));

		IfExp callPostCondition = new IfExp(new StaticCallExp(Evaluator.beforePost, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), getReturnTypeExp(contractBehavior),
				getReturnValueExp(affectedBehavior)));
		callPostCondition.addIfBody(callContractPost);
		return callPostCondition;
	}

	private IfExp getPreConditionCall(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException {
		TryExp callContractPre = new TryExp(getContractCallExp(contractInfo.getContractClass(), affectedClass,
				contractBehavior));
		callContractPre.addCatch(AssertionError.class,
				getAfterContractMethodCall().append(new ThrowExp(callContractPre.getCatchClauseVar(1))));
		callContractPre.addCatch(Throwable.class, getAfterContractMethodCall());
		callContractPre.addFinally(getAfterContractCall());
		IfExp callPreCondition = new IfExp(new StaticCallExp(Evaluator.beforePre, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), getReturnTypeExp(contractBehavior)));
		callPreCondition.addIfBody(callContractPre);
		return callPreCondition;
	}

	private StandaloneExp getAfterContractMethodCall() {
		StandaloneExp afterContractMethod = new StaticCallExp(Evaluator.afterContractMethod).toStandalone();
		return afterContractMethod;
	}

	private NestedExp getReturnValueExp(CtBehavior affectedBehavior) throws NotFoundException {
		if (!(affectedBehavior instanceof CtMethod)
				|| ((CtMethod) affectedBehavior).getReturnType().equals(CtClass.voidType)) {
			return NestedExp.NULL;
		}
		return new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
	}

	CtBehavior getAffectedBehavior(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException, CannotCompileException {
		CtBehavior affectedBehavior = null;
		if (contractBehavior.hasAnnotation(ClassInvariant.class)) {
			return null;
		}
		if (isConstructor(contractBehavior)) {
			affectedBehavior = getAffectedConstructor(contractInfo, affectedClass, contractBehavior);
		} else if (contractBehavior instanceof CtMethod) {
			affectedBehavior = getAffectedMethod(contractInfo, affectedClass, contractBehavior);
		} else {
			throw new TransformationException("contractBehavior " + contractBehavior.getLongName()
					+ " is neither constructor nor method");
		}
		return affectedBehavior;
	}

	CtMethod getAffectedMethod(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException, CannotCompileException {
		CtClass currentClass = affectedClass;
		CtMethod affectedMethod = null;
		while (affectedMethod == null && currentClass != null) {
			try {
				affectedMethod = currentClass.getDeclaredMethod(contractBehavior.getName(),
						contractBehavior.getParameterTypes());
			} catch (NotFoundException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		if (affectedMethod == null) {
			logger.warn("could not find a matching method in affected class " + affectedClass.getName()
					+ " for method '" + contractBehavior.getName() + "' in contract class "
					+ contractInfo.getContractClass().getName());
			return null;
		}
		if (!affectedMethod.getDeclaringClass().equals(affectedClass)) {
			logger.warn("could not find method " + contractBehavior.getName() + " in affected class "
					+ affectedClass.getName() + " for contract class " + contractInfo.getContractClass().getName()
					+ " - inserting an empty method");
			affectedMethod = CtNewMethod.delegator(affectedMethod, affectedClass);
			affectedMethod.setModifiers(Modifier.clear(affectedMethod.getModifiers(), Modifier.NATIVE));
			affectedMethod.setModifiers(Modifier.clear(affectedMethod.getModifiers(), Modifier.ABSTRACT));
			affectedClass.addMethod(affectedMethod);
		}
		return affectedMethod;
	}

	CtConstructor getAffectedConstructor(ContractInfo contractInfo, CtClass affectedClass, CtBehavior contractBehavior) {
		if (contractInfo.getTargetClass().isInterface()) {
			return null;
		}
		CtConstructor affectedConstructor;
		try {
			affectedConstructor = affectedClass.getDeclaredConstructor(getConstructorParameterTypes(affectedClass,
					contractBehavior));
		} catch (NotFoundException e) {
			logger.warn("could not find a matching constructor in affected class " + affectedClass.getName()
					+ " for the constructor defined in contract class " + contractInfo.getContractClass().getName());
			return null;
		}
		if (contractBehavior instanceof CtMethod) {
			return affectedConstructor;
		}
		try {
			contractInfo.getContractClass().getDeclaredMethod(ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME,
					contractBehavior.getParameterTypes());
			return null;
		} catch (NotFoundException e) {
			return affectedConstructor;
		}
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

	private ArrayExp getArgsArray(CtClass affectedClass, CtBehavior contractBehavior) throws NotFoundException {
		if (isConstructor(contractBehavior) && constructorHasAdditionalParameter(affectedClass)) {
			return ArrayExp.forArgs(contractBehavior, 2);
		}
		return ArrayExp.forArgs(contractBehavior);
	}
}
