package de.vksi.c4j.systemtest.old;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldWithMultipleContractsSystemTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testOldWithMultipleContracts() {
		new DummyClass().method();
	}

	@ContractReference(DummyContract.class)
	public static class DummyClass extends SuperClass {
		@Override
		public void method() {
		}

		@Pure
		public int getOne() {
			return 1;
		}
	}

	public static class DummyContract extends DummyClass {
		@Target
		private DummyClass target;

		@Override
		public void method() {
			if (postCondition()) {
				assert old(target.getOne()) == 1;
			}
		}
	}

	@ContractReference(SuperContract.class)
	public static class SuperClass {
		public void method() {
		}

		@Pure
		public int getTwo() {
			return 2;
		}
	}

	public static class SuperContract extends SuperClass {
		@Target
		private SuperClass target;

		@Override
		public void method() {
			if (postCondition()) {
				assert old(target.getTwo()) == 2;
			}
		}
	}
}
