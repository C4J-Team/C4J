package de.andrena.next.internal.transformer;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import de.andrena.next.ClassInvariant;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.Evaluator;
import de.andrena.next.internal.compiler.EmptyExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ValueExp;

public class ClassInvariantTransformer extends AbstractTargetClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo) throws Exception {
		List<CtMethod> classInvariantMethods = new ArrayList<CtMethod>();
		for (CtMethod contractMethod : contractInfo.getContractClass().getDeclaredMethods()) {
			if (contractMethod.hasAnnotation(ClassInvariant.class)) {
				classInvariantMethods.add(contractMethod);
			}
		}
		if (!classInvariantMethods.isEmpty()) {
			transformTargetMethods(contractInfo, classInvariantMethods);
		}
	}

	private void transformTargetMethods(ContractInfo contractInfo, List<CtMethod> classInvariantMethods)
			throws CannotCompileException {
		StandaloneExp callInvariantExpression = callInvariantExpression(contractInfo, classInvariantMethods);
		for (CtBehavior behavior : contractInfo.getTargetClass().getDeclaredBehaviors()) {
			callInvariantExpression.insertAfter(behavior);
		}
	}

	private StandaloneExp callInvariantExpression(ContractInfo contractInfo, List<CtMethod> classInvariantMethods) {
		StandaloneExp callInvariantExpression = new EmptyExp();
		for (CtMethod classInvariantMethod : classInvariantMethods) {
			callInvariantExpression = callInvariantExpression.append(new StaticCallExp(Evaluator.callInvariant,
					NestedExp.THIS, new ValueExp(contractInfo.getContractClass()), new ValueExp(classInvariantMethod
							.getName())));
		}
		return callInvariantExpression;
	}

}
