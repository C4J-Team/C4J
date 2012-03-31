package de.andrena.c4j.internal.transformer;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.c4j.internal.compiler.CastExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.Evaluator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.ReflectionHelper;

public abstract class AffectedClassTransformerForSingleContract extends AbstractAffectedClassTransformer {
	private ReflectionHelper reflectionHelper = new ReflectionHelper();

	@Override
	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		for (ContractInfo contractInfo : contracts) {
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
