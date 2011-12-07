package next.internal.transformer;

import javassist.CtClass;
import javassist.CtMethod;

public abstract class TargetDeclaredMethodTransformer extends ClassTransformer {

	@Override
	public void transform(CtClass targetClass, CtClass contractClass) throws Exception {
		for (CtMethod targetMethod : targetClass.getDeclaredMethods()) {
			transform(targetMethod, contractClass);
		}
	}

	public abstract void transform(CtMethod targetMethod, CtClass contractClass) throws Exception;

}
