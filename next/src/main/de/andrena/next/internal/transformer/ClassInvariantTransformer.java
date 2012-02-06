package de.andrena.next.internal.transformer;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.ClassInvariant;
import de.andrena.next.internal.compiler.EmptyExp;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.TryExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ClassInvariantTransformer extends AffectedClassTransformerForSingleContract {

	@Override
	public void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception {
		List<CtMethod> classInvariantMethods = new ArrayList<CtMethod>();
		for (CtMethod contractMethod : contractInfo.getContractClass().getDeclaredMethods()) {
			if (contractMethod.hasAnnotation(ClassInvariant.class)) {
				classInvariantMethods.add(contractMethod);
			}
		}
		if (!classInvariantMethods.isEmpty()) {
			transformTargetMethods(contractInfo.getContractClass(), affectedClass, classInvariantMethods);
		}
	}

	private void transformTargetMethods(CtClass contractClass, CtClass affectedClass,
			List<CtMethod> classInvariantMethods) throws CannotCompileException, NotFoundException {
		StandaloneExp callInvariantExpression = getInvariantCall(contractClass, affectedClass, classInvariantMethods);
		logger.info("classInvariant after: " + callInvariantExpression.getCode());
		for (CtBehavior behavior : affectedClass.getDeclaredBehaviors()) {
			callInvariantExpression.insertAfter(behavior);
		}
	}

	private StandaloneExp callInvariantExpression(CtClass contractClass, CtClass affectedClass,
			List<CtMethod> classInvariantMethods) {
		StandaloneExp callInvariantExpression = new EmptyExp();
		for (CtMethod classInvariantMethod : classInvariantMethods) {
			callInvariantExpression = callInvariantExpression.append(new StaticCallExp(Evaluator.callInvariant,
					NestedExp.THIS, new ValueExp(contractClass), new ValueExp(affectedClass), new ValueExp(
							classInvariantMethod.getName())));
		}
		return callInvariantExpression;
	}

	private IfExp getInvariantCall(CtClass contractClass, CtClass affectedClass, List<CtMethod> classInvariantMethods)
			throws NotFoundException {
		StandaloneExp invariantCalls = new EmptyExp();
		for (CtMethod classInvariantMethod : classInvariantMethods) {
			invariantCalls = invariantCalls.append(getContractCallExp(contractClass, affectedClass,
					classInvariantMethod));
		}
		TryExp tryInvariants = new TryExp(invariantCalls);
		tryInvariants.addFinally(getAfterContractCall());
		IfExp invariantCondition = new IfExp(new StaticCallExp(Evaluator.beforeInvariant, NestedExp.THIS, new ValueExp(
				contractClass)));
		invariantCondition.addIfBody(tryInvariants);
		return invariantCondition;
	}
}
