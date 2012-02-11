package de.andrena.next.systemtest.inheritance;

import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class SuperclassSystemTest {
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
				.expectLogWarning("could not find method method in affected class de.andrena.next.systemtest.inheritance.SuperclassSystemTest$DummyClass"
						+ " for contract class de.andrena.next.systemtest.inheritance.SuperclassSystemTest$DummyContract - inserting an empty method");
		dummy.method(5);
	}

	@Test
	public void testNoWarningWhenContractMethodNotOverwritten() {
		transformerAware
				.banLogWarning("could not find method method in affected class de.andrena.next.systemtest.inheritance.SuperclassSystemTest$NoWarningClass"
						+ " for contract class de.andrena.next.systemtest.inheritance.SuperclassSystemTest$SuperContract - inserting an empty method");
		new NoWarningClass().method(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFailsInSuperClassForDummyClassDeclaringMethod() {
		new DummyClassDeclaringMethod().method(0);
	}

	@Contract(NoWarningClassContract.class)
	public static class NoWarningClass extends SuperClass {
	}

	public static class NoWarningClassContract extends NoWarningClass {
	}

	@Contract(DummyContract.class)
	public static class DummyClass extends SuperClass {
	}

	public static class DummyContract extends DummyClass {
		@Override
		public void method(final int arg) {
			if (pre()) {
				assert arg < 5;
			}
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
			if (pre()) {
				assert arg < 5;
			}
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
			if (pre()) {
				assert arg > 0;
			}
		}
	}
}
