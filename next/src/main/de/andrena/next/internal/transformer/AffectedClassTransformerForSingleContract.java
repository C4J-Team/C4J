package de.andrena.next.internal.transformer;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;
import de.andrena.next.internal.util.HelperFactory;
import de.andrena.next.internal.util.ListOrderedSet;
import de.andrena.next.internal.util.ReflectionHelper;

public abstract class AffectedClassTransformerForSingleContract extends AbstractAffectedClassTransformer {
	private ReflectionHelper reflectionHelper = HelperFactory.getReflectionHelper();

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
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
		return getContractInstance.appendCall(reflectionHelper.getContractBehaviorName(contractBehavior),
				getArgsList(affectedClass, contractBehavior)).toStandalone();
	}

	protected List<NestedExp> getArgsList(CtClass affectedClass, CtBehavior contractBehavior) throws NotFoundException {
		if (reflectionHelper.isContractConstructor(contractBehavior)
				&& reflectionHelper.constructorHasAdditionalParameter(affectedClass)) {
			return NestedExp.getArgsList(contractBehavior, 2);
		}
		return NestedExp.getArgsList(contractBehavior, 1);
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
