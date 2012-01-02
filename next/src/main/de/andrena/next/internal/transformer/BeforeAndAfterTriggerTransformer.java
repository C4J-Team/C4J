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

public class BeforeAndAfterTriggerTransformer extends AffectedDeclaredBehaviorTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtBehavior affectedBehavior) throws Exception {
		String contractBehaviorName;
		try {
			if (affectedBehavior instanceof CtMethod) {
				contractInfo.getContractClass().getDeclaredMethod(affectedBehavior.getName(),
						affectedBehavior.getParameterTypes());
				contractBehaviorName = affectedBehavior.getName();
			} else if (affectedBehavior instanceof CtConstructor) {
				contractInfo.getContractClass().getDeclaredConstructor(affectedBehavior.getParameterTypes());
				contractBehaviorName = ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME;
			} else {
				return;
			}
		} catch (NotFoundException e) {
			return;
		}
		logger.info("transforming method " + affectedBehavior.getLongName());
		ArrayExp paramTypesArray = ArrayExp.forParamTypes(affectedBehavior);
		ArrayExp argsArray = ArrayExp.forArgs(affectedBehavior);
		StaticCallExp callBefore = new StaticCallExp(Evaluator.before, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), new ValueExp(contractBehaviorName), paramTypesArray, argsArray);
		NestedExp returnValue = new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
		if (!(affectedBehavior instanceof CtMethod)
				|| ((CtMethod) affectedBehavior).getReturnType().equals(CtClass.voidType)) {
			returnValue = NestedExp.NULL;
		}
		StaticCallExp callAfter = new StaticCallExp(Evaluator.after, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), new ValueExp(contractBehaviorName), paramTypesArray, argsArray,
				returnValue);
		callBefore.toStandalone().insertBefore(affectedBehavior);
		callAfter.toStandalone().insertAfter(affectedBehavior);
	}
}
