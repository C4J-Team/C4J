package de.andrena.c4j.systemtest.precondition;

import static de.andrena.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Contract;

public class PreConditionInConstructorSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPreCondition() {
		new DummyClass(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFails() {
		new DummyClass(0);
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		public DummyClass(int arg) {
		}
	}

	public static class DummyContract extends DummyClass {
		public DummyContract(final int arg) {
			super(arg);
			if (pre()) {
				assert arg > 0;
			}
		}
	}
}
