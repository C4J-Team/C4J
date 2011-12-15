package next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;

public abstract class ContractDeclaredBehaviorTransformer extends ClassTransformer {

	@Override
	public void transform(CtClass targetClass, CtClass contractClass) throws Exception {
		for (CtBehavior contractBehavior : contractClass.getDeclaredBehaviors()) {
			logger.info("transforming behavior " + contractBehavior.getLongName());
			transform(contractBehavior, targetClass);
		}
	}

	public abstract void transform(CtBehavior contractBehavior, CtClass targetClass) throws Exception;

}
