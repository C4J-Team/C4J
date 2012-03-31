package de.andrena.c4j.systemtest.precondition;

import static de.andrena.c4j.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ContractReference;

public class PreConditionSystemTest {
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
	public void testPreConditionFails() {
		dummy.method(0);
	}

	@ContractReference(DummyContract.class)
	public static class DummyClass {
		public void method(int arg) {
		}
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void method(final int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}
}
