package next.systemtest.pre;

import next.Contract;

import org.junit.Before;
import org.junit.Test;

import static next.Condition.pre;

public class PreConditionSystemTest {

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
			pre();
			assert arg > 0;
		}
	}
}
