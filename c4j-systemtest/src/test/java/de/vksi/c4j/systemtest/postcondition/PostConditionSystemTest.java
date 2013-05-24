package de.vksi.c4j.systemtest.postcondition;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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

	@ContractReference(DummyContract.class)
	private static class DummyClass {
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

	private static class DummyContract extends DummyClass {
		@Override
		public void setStaticValue(int value) {
			if (postCondition()) {
				assert DummyClass.staticValue == 5;
			}
		}

		@Override
		public int noArgs() {
			if (postCondition()) {
				assert DummyClass.staticValue == 5;
			}
			return (Integer) ignored();
		}

		@Override
		public int returnValue(int value) {
			if (postCondition()) {
				assert result(int.class) == 5;
			}
			return (Integer) ignored();
		}

		@Override
		public void returnValueVoid() {
			if (postCondition()) {
				assert result() == null;
			}
		}
	}
}
