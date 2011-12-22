package de.andrena.next.internal.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.Evaluator;
import de.andrena.next.internal.compiler.ArrayExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.util.ObjectConverter;

public class BeforeAndAfterTriggerTransformer extends TargetDeclaredMethodTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtMethod targetMethod) throws Exception {
		try {
			contractInfo.getContractClass().getDeclaredMethod(targetMethod.getName(), targetMethod.getParameterTypes());
		} catch (NotFoundException e) {
			return;
		}
		logger.info("transforming method " + targetMethod.getLongName());
		ArrayExp paramTypesArray = ArrayExp.forParamTypes(targetMethod);
		ArrayExp argsArray = ArrayExp.forArgs(targetMethod);
		StaticCallExp callBefore = new StaticCallExp(Evaluator.before, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), new ValueExp(targetMethod.getName()), paramTypesArray, argsArray);
		NestedExp returnValue = new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
		if (targetMethod.getReturnType().equals(CtClass.voidType)) {
			returnValue = NestedExp.NULL;
		}
		StaticCallExp callAfter = new StaticCallExp(Evaluator.after, NestedExp.THIS, new ValueExp(
				contractInfo.getContractClass()), new ValueExp(targetMethod.getName()), paramTypesArray, argsArray,
				returnValue);
		callBefore.toStandalone().insertBefore(targetMethod);
		callAfter.toStandalone().insertAfter(targetMethod);
	}
}
