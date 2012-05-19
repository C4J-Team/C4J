package de.andrena.c4j.systemtest.config.defaultpreconditiontrue;

import static de.andrena.c4j.Condition.pre;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class DefaultPreConditionTrueSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		transformerAwareRule.expectGlobalLog(Level.ERROR,
				"Found strengthening pre-condition in" + " " + ContractClass.class.getName() + ".method(int)"
						+ " which is already defined from " + SuperClass.class.getName()
						+ " - ignoring the pre-condition.");
		new TargetClass().method(-1);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass extends SuperClass {
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}

	public static class SuperClass {
		public void method(int arg) {
		}
	}

}
