package next.systemtest.pre;

import static next.Condition.pre;
import next.Contract;

import org.junit.Test;

public class PreConditionSystemTest {

	@Test
	public void testPreCondition() {
		new DummyClass().method(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFails() {
		new DummyClass().method(0);
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		public void method(int arg) {

		}
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void method(int arg) {
			pre(arg > 0);
		}
	}
}
