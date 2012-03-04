package de.andrena.c4j.systemtest.postcondition;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Contract;

public class PostConditionSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

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

		public void returnValueVoid() {
		}
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void setStaticValue(int value) {
			if (post()) {
				assert DummyClass.staticValue == 5;
			}
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
