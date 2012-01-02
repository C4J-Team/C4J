package de.andrena.next.systemtest.dbi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition.PreCondition;
import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

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

	@Contract(DummyContract.class)
	public static class DummyClass {
		public void method(int arg) {
		}
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void method(final int arg) {
			new PreCondition() {
				{
					assert arg > 0;
				}
			};
		}
	}
}
