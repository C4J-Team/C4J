package de.andrena.next.systemtest.precondition;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition.PreCondition;
import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class PreConditionInSuperclassSystemTest {
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
	public void testPreConditionFailsInSuperClass() {
		dummy.method(0);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFailsInDummyClass() {
		transformerAware
				.expectLogWarning("could not find method method in affected class de.andrena.next.systemtest.precondition.PreConditionInSuperclassSystemTest$DummyClass"
						+ " for contract class de.andrena.next.systemtest.precondition.PreConditionInSuperclassSystemTest$DummyContract - inserting an empty method");
		dummy.method(5);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFailsInSuperClassForDummyClassDeclaringMethod() {
		new DummyClassDeclaringMethod().method(0);
	}

	@Contract(DummyContract.class)
	public static class DummyClass extends SuperClass {
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void method(final int arg) {
			new PreCondition() {
				{
					assert arg < 5;
				}
			};
		}
	}

	@Contract(DummyContractDeclaringMethod.class)
	public static class DummyClassDeclaringMethod extends SuperClass {
		@Override
		public void method(int arg) {
		}
	}

	public static class DummyContractDeclaringMethod extends DummyClassDeclaringMethod {
		@Override
		public void method(final int arg) {
			new PreCondition() {
				{
					assert arg < 5;
				}
			};
		}
	}

	@Contract(SuperContract.class)
	public static class SuperClass {
		public void method(int arg) {
		}
	}

	public static class SuperContract extends SuperClass {
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
