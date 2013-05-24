package de.vksi.c4j.systemtest.unchanged;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.unchanged;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class UnchangedAndOldTogetherSystemTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void test() {
		new TargetClass().method();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method() {
		}

		@Pure
		public int getOne() {
			return 1;
		}

		@Pure
		public int getTwo() {
			return 2;
		}
	}

	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method() {
			if (postCondition()) {
				assert old(target.getOne()) == 1;
				assert unchanged(target.getTwo());
			}
		}
	}
}
