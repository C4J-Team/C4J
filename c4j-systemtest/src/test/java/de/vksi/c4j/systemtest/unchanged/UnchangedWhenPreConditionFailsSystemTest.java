package de.vksi.c4j.systemtest.unchanged;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static de.vksi.c4j.Condition.unchanged;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class UnchangedWhenPreConditionFailsSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void test() {
		try {
			new TargetClass().method();
		} catch (AssertionError e) {
			// expected
		}
		// will fail because PureEvaluator.pureCallDepth is wrong
		TargetClass.staticMethod();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected static int field;

		public static void staticMethod() {
			field = 3;
		}

		public void method() {
		}

		@Pure
		public String getValue() {
			return "abc";
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method() {
			if (preCondition()) {
				assert false;
			}
			if (postCondition()) {
				assert unchanged(target.getValue());
			}
		}
	}
}
