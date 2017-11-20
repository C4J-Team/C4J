package de.vksi.c4j.systemtest.config.defaultpreconditionundefinedexternalclass;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Contract;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class DefaultPreConditionShouldBeUndefinedInExternalContractsSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testPreConditionUndefinedWithExternalContract() {
		new TargetClass().method(-1);
	}

	private static class TargetClass extends SuperClass {
	}

	@Contract(forTarget = TargetClass.class)
	private static class ContractClass extends TargetClass {
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@Contract(forTarget = SuperClass.class)
	private static class SuperContractClass extends SuperClass {
		public void method(int arg) {
		}
	}
	
	private static class SuperClass {
		public void method(int arg) {
		}
	}
}
