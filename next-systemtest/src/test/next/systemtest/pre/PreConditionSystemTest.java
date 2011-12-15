package next.systemtest.pre;

import static next.Condition.pre;
import next.Contract;
import next.systemtest.TransformerAwareTest;

import org.junit.Before;
import org.junit.Test;

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
