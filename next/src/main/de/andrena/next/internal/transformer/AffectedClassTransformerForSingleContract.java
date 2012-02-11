package de.andrena.next.internal.transformer;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public abstract class AffectedClassTransformerForSingleContract extends AbstractAffectedClassTransformer {

	@Override
	public void transform(List<CtClass> involvedClasses, List<ContractInfo> contracts, CtClass affectedClass)
			throws Exception {
		for (ContractInfo contractInfo : contracts) {
			logger.info("transforming class " + affectedClass.getName() + " with contract-class "
					+ contractInfo.getContractClass().getName() + " from target-class "
					+ contractInfo.getTargetClass().getName());
			transform(contractInfo, affectedClass);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception;

	protected StandaloneExp getContractCallExp(CtClass contractClass, CtClass affectedClass, CtBehavior contractBehavior)
			throws NotFoundException {
		CastExp getContractInstance = new CastExp(contractClass, new StaticCallExp(Evaluator.getContractFromCache,
				NestedExp.THIS, new ValueExp(contractClass), new ValueExp(affectedClass)));
		return getContractInstance.appendCall(getContractBehaviorName(contractBehavior),
				getArgsList(affectedClass, contractBehavior)).toStandalone();
	}

	protected List<NestedExp> getArgsList(CtClass affectedClass, CtBehavior contractBehavior) throws NotFoundException {
		if (isConstructor(contractBehavior) && constructorHasAdditionalParameter(affectedClass)) {
			return NestedExp.getArgsList(contractBehavior, 2);
		}
		return NestedExp.getArgsList(contractBehavior, 1);
	}

	boolean isConstructor(CtBehavior contractBehavior) {
		return contractBehavior instanceof CtConstructor
				|| contractBehavior.getName().equals(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	protected boolean constructorHasAdditionalParameter(CtClass affectedClass) throws NotFoundException {
		return affectedClass.getDeclaringClass() != null && !Modifier.isStatic(affectedClass.getModifiers());
	}

	String getContractBehaviorName(CtBehavior contractBehavior) {
		String contractBehaviorName;
		if (isConstructor(contractBehavior)) {
			contractBehaviorName = ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME;
		} else {
			contractBehaviorName = contractBehavior.getName();
		}
		return contractBehaviorName;
	}

	protected StandaloneExp getAfterContractCall() {
		StandaloneExp afterContractExp = new StaticCallExp(Evaluator.afterContract).toStandalone();
		return afterContractExp;
	}

	protected NestedExp getReturnTypeExp(CtBehavior contractBehavior) throws NotFoundException {
		NestedExp returnTypeExp = NestedExp.NULL;
		if (contractBehavior instanceof CtMethod) {
			returnTypeExp = new ValueExp(((CtMethod) contractBehavior).getReturnType());
		}
		return returnTypeExp;
	}
}
