package de.andrena.next.systemtest.dbi;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition.PreCondition;
import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

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
			new PreCondition() {
				{
					assert arg > 0;
				}
			};
		}
	}
}
