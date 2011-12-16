package de.andrena.next.systemtest.pre;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareTest;
import static de.andrena.next.Condition.pre;

public class PreConditionSystemTest extends TransformerAwareTest {

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
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}
}
