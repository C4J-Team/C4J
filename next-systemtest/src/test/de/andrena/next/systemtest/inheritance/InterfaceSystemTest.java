package de.andrena.next.systemtest.inheritance;

import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

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

	public static class DummyClass implements ContractedInterface {
		@Override
		public void method(int value) {
		}
	}

	@Contract(ContractedInterfaceContract.class)
	public interface ContractedInterface {
		void method(int value);
	}

	public static class ContractedInterfaceContract implements ContractedInterface {
		@Override
		public void method(int value) {
			if (pre()) {
				assert value > 0;
			}
		}
	}
}
