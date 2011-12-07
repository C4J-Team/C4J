package next.internal.transformer;

import javassist.CtClass;
import javassist.CtMethod;

public abstract class ContractDeclaredMethodTransformer extends ClassTransformer {

	@Override
	public void transform(CtClass targetClass, CtClass contractClass) throws Exception {
		for (CtMethod contractMethod : contractClass.getDeclaredMethods()) {
			transform(contractMethod, targetClass);
		}
	}

	public abstract void transform(CtMethod contractMethod, CtClass targetClass) throws Exception;

}
