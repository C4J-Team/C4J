package de.vksi.c4j.systemtest.inheritance;

import static de.vksi.c4j.Condition.postCondition;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
	public void testPostConditionFailsInSuperClass() {
		dummy.method(0);
	}

	@Test(expected = AssertionError.class)
	public void testPostConditionFailsInDummyClass() {
		transformerAware.expectGlobalLog(Level.WARN, "could not find method method in affected class "
				+ DummyClass.class.getName() + " for contract class " + DummyContract.class.getName()
				+ " - inserting an empty method");
		dummy.method(5);
	}

	@Test
	public void testErrorWhenOverriddenTargetMethodIsFinal() {
		transformerAware.expectGlobalLog(Level.WARN, "could not find method method in affected class "
				+ FinalMethodOverriddenClass.class.getName() + " for contract class "
				+ FinalMethodOverriddenContract.class.getName()
				+ " and cannot insert a delegate, as the overridden method is final");
		new FinalMethodOverriddenClass().method(5);
	}

	@Test
	public void testNoWarningWhenContractMethodNotOverwritten() {
		transformerAware.banGlobalLog(Level.WARN, "could not find method method in affected class "
				+ NoWarningClass.class.getName() + " for contract class " + SuperContract.class.getName()
				+ " - inserting an empty method");
		new NoWarningClass().method(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFailsInSuperClassForDummyClassDeclaringMethod() {
		new DummyClassDeclaringMethod().method(0);
	}

	@ContractReference(NoWarningClassContract.class)
	private static class NoWarningClass extends SuperClass {
	}

	private static class NoWarningClassContract extends NoWarningClass {
	}

	private static class FinalSuperClass {
		public final void method(int arg) {
		}
	}

	@ContractReference(FinalMethodOverriddenContract.class)
	private static class FinalMethodOverriddenClass extends FinalSuperClass {
	}

	@SuppressWarnings("unused")
	private static class FinalMethodOverriddenContract {
		public void method(int arg) {
			if (postCondition()) {
				assert arg < 5;
			}
		}
	}

	@ContractReference(DummyContract.class)
	private static class DummyClass extends SuperClass {
	}

	private static class DummyContract extends DummyClass {
		@Override
		public void method(final int arg) {
			if (postCondition()) {
				assert arg < 5;
			}
		}
	}

	@ContractReference(DummyContractDeclaringMethod.class)
	private static class DummyClassDeclaringMethod extends SuperClass {
		@Override
		public void method(int arg) {
		}
	}

	private static class DummyContractDeclaringMethod extends DummyClassDeclaringMethod {
		@Override
		public void method(final int arg) {
			if (postCondition()) {
				assert arg < 5;
			}
		}
	}

	@ContractReference(SuperContract.class)
	private static class SuperClass {
		public void method(int arg) {
		}
	}

	private static class SuperContract extends SuperClass {
		@Override
		public void method(final int arg) {
			if (postCondition()) {
				assert arg > 0;
			}
		}
	}
}
