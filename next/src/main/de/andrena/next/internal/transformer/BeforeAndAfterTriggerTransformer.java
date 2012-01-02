package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.Evaluator;
import de.andrena.next.internal.compiler.ArrayExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.util.ObjectConverter;

public class BeforeAndAfterTriggerTransformer extends TargetDeclaredBehaviorTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior targetBehavior) throws Exception {
		String contractBehaviorName;
		try {
			if (targetBehavior instanceof CtMethod) {
				contractInfo.getContractClass().getDeclaredMethod(targetBehavior.getName(),
						targetBehavior.getParameterTypes());
				contractBehaviorName = targetBehavior.getName();
			} else if (targetBehavior instanceof CtConstructor) {
				contractInfo.getContractClass().getDeclaredConstructor(targetBehavior.getParameterTypes());
				contractBehaviorName = ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME;
			} else {
				return;
			}
		} catch (NotFoundException e) {
			return;
		}
		logger.info("transforming method " + targetBehavior.getLongName());
		ArrayExp paramTypesArray = ArrayExp.forParamTypes(targetBehavior);
		ArrayExp argsArray = ArrayExp.forArgs(targetBehavior);
		StaticCallExp callBefore = new StaticCallExp(Evaluator.before, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), new ValueExp(contractBehaviorName), paramTypesArray, argsArray);
		NestedExp returnValue = new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
		if (!(targetBehavior instanceof CtMethod)
				|| ((CtMethod) targetBehavior).getReturnType().equals(CtClass.voidType)) {
			returnValue = NestedExp.NULL;
		}
		StaticCallExp callAfter = new StaticCallExp(Evaluator.after, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), new ValueExp(contractBehaviorName), paramTypesArray, argsArray,
				returnValue);
		callBefore.toStandalone().insertBefore(targetBehavior);
		callAfter.toStandalone().insertAfter(targetBehavior);
	}
}
