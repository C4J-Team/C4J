package de.vksi.c4j.systemtest.inheritance;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class InterfaceSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testPreCondition() {
		dummy.method(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFailsInSuperClass() {
		dummy.method(0);
	}

	private static class DummyClass implements ContractedInterface {
		@Override
		public void method(int value) {
		}
	}

	@ContractReference(ContractedInterfaceContract.class)
	public interface ContractedInterface {
		void method(int value);
	}

	private static class ContractedInterfaceContract implements ContractedInterface {
		@Override
		public void method(int value) {
			if (preCondition()) {
				assert value > 0;
			}
		}
	}
}
