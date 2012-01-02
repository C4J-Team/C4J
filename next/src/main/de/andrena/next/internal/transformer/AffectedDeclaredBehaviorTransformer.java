package de.andrena.next.internal.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public abstract class AffectedDeclaredBehaviorTransformer extends AbstractAffectedClassTransformer {

	@Override
	public void transform(ContractInfo contractInfo, CtClass affectedClass) throws Exception {
		for (CtBehavior affectedBehavior : affectedClass.getDeclaredBehaviors()) {
			transform(contractInfo, affectedBehavior);
		}
	}

	public abstract void transform(ContractInfo contractInfo, CtBehavior affectedBehavior) throws Exception;

}
