package next.systemtest.post;

import static next.Condition.ignored;
import static next.Condition.post;
import static next.Condition.result;
import next.Condition.PostCondition;
import next.Contract;
import next.systemtest.TransformerAwareTest;

import org.junit.Test;

public class PostConditionSystemTest extends TransformerAwareTest {

	@Test
	public void testPostCondition() {
		new DummyClass().setStaticValue(5);
	}

	@Test(expected = AssertionError.class)
	public void testPostConditionFails() {
		new DummyClass().setStaticValue(4);
	}

	@Test
	public void testPostNoArgs() {
		new DummyClass().noArgs();
	}

	@Test
	public void testPostConditionWithReturnValue() {
		new DummyClass().returnValue(5);
	}

	@Test(expected = AssertionError.class)
	public void testPostConditionWithReturnValueFails() {
		new DummyClass().returnValue(4);
	}

	@Test
	public void testPostConditionWithReturnValueAndVoid() {
		new DummyClass().returnValueVoid();
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		protected static int staticValue;

		public void setStaticValue(int value) {
			DummyClass.staticValue = value;
		}

		public int noArgs() {
			DummyClass.staticValue = 5;
			return 0;
		}

		public int returnValue(int value) {
			return value;
		}

		public void returnValueVoid() {}
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void setStaticValue(int value) {
			new PostCondition() {
				{
					assert DummyClass.staticValue == 5;
				}
			};
		}

		@Override
		public int noArgs() {
			if (post()) {
				assert DummyClass.staticValue == 5;
			}
			return ignored();
		}

		@Override
		public int returnValue(int value) {
			if (post()) {
				assert result(int.class) == 5;
			}
			return ignored();
		}

		@Override
		public void returnValueVoid() {
			if (post()) {
				assert result() == null;
			}
		}
	}
}
