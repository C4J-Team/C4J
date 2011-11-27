package next.systemtest.post;

import next.Contract;

import org.junit.Test;

import static next.Condition.post;
import static next.Condition.result;

public class PostConditionSystemTest {

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
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void setStaticValue(int value) {
			post(DummyClass.staticValue == 5);
		}

		@Override
		public int noArgs() {
			post(DummyClass.staticValue == 5);
			return 0;
		}

		@Override
		public int returnValue(int value) {
			System.out.println("before post");
			post(result(int.class) == 5);
			System.out.println("after post");
			return 0;
		}
	}
}
