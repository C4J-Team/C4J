package next.internal.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import next.internal.Evaluator;
import next.internal.compiler.ArrayExp;
import next.internal.compiler.NestedExp;
import next.internal.compiler.StaticCallExp;
import next.internal.compiler.ValueExp;
import next.internal.util.ObjectConverter;

public class BeforeAndAfterTriggerTransformer extends TargetDeclaredMethodTransformer {

	@Override
	public void transform(CtMethod targetMethod, CtClass contractClass) throws Exception {
		try {
			contractClass.getDeclaredMethod(targetMethod.getName(), targetMethod.getParameterTypes());
		} catch (NotFoundException e) {
			return;
		}
		logger.info("transforming method " + targetMethod.getLongName());
		ArrayExp paramTypesArray = ArrayExp.forParamTypes(targetMethod);
		ArrayExp argsArray = ArrayExp.forArgs(targetMethod);
		StaticCallExp callBefore = new StaticCallExp(Evaluator.before, NestedExp.THIS, new ValueExp(contractClass),
				new ValueExp(targetMethod.getName()), paramTypesArray, argsArray);
		NestedExp returnValue = new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
		if (targetMethod.getReturnType().equals(CtClass.voidType)) {
			returnValue = NestedExp.NULL;
		}
		StaticCallExp callAfter = new StaticCallExp(Evaluator.after, NestedExp.THIS, new ValueExp(contractClass),
				new ValueExp(targetMethod.getName()), paramTypesArray, argsArray, returnValue);
		callBefore.toStandalone().insertBefore(targetMethod);
		callAfter.toStandalone().insertAfter(targetMethod);
	}
}
