package de.andrena.next.internal.transformer;

import java.util.Collection;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.Pure;
import de.andrena.next.internal.editor.PureMethodExpressionEditor;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class PureTransformer extends AbstractAffectedClassTransformer {

	@Override
	public void transform(Collection<ContractInfo> contractInfos, CtClass affectedClass) throws Exception {
		for (CtBehavior affectedBehavior : affectedClass.getDeclaredBehaviors()) {
			if (affectedBehavior.hasAnnotation(Pure.class)) {
				verifyPure(affectedBehavior);
			}
		}
	}

	private void verifyPure(CtBehavior affectedBehavior) throws CannotCompileException {
		affectedBehavior.instrument(new PureMethodExpressionEditor(affectedBehavior));
	}

}
