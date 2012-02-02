package de.andrena.next.systemtest.contractclassmagic;

import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.Target;
import de.andrena.next.systemtest.TransformerAwareRule;

public class FieldAccessSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testPreConditionWithFieldAccess() {
		dummy.setValue(5);
		dummy.methodContractHasFieldAccess();
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		protected int value;

		public void methodContractHasFieldAccess() {
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class DummyContract extends DummyClass {
		@Target
		private DummyClass target;

		@Override
		public void methodContractHasFieldAccess() {
			if (pre()) {
				assert target.value == 5;
			}
		}
	}

	@Test
	public void testFieldAccessWithSameFieldInSuperClass() {
		new TargetClass().method();
	}

	@Contract(TargetClassContract.class)
	public static class TargetClass extends SuperClass {
		@Override
		public void method() {
		}
	}

	public static class TargetClassContract extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method() {
			if (pre()) {
				assert target.field == 0;
			}
		}
	}

	@Contract(SuperClassContract.class)
	public static class SuperClass {
		protected int field;

		public void method() {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Target
		private SuperClass target;

		@Override
		public void method() {
			if (pre()) {
				assert target.field == 0;
			}
		}
	}
}
