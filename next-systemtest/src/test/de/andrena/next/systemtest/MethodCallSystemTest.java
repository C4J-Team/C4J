package de.andrena.next.systemtest;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;

public class MethodCallSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testPreConditionWithMethodAccess() {
		dummy.setValue(5);
		dummy.methodContractHasMethodAccess();
	}

	@Test
	public void testPreConditionWithMethodAccessAndMethodAlsoInContract() {
		dummy.setValue(5);
		dummy.methodContractHasMethodAccessAndMethodAlsoInContract();
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		protected int value;

		public void setValue(int value) {
			this.value = value;
		}

		protected int getValue() {
			return value;
		}

		protected int getValueAlsoInContract() {
			return value;
		}

		public void methodContractHasMethodAccess() {
		}

		public void methodContractHasMethodAccessAndMethodAlsoInContract() {
		}

	}

	public static class DummyContract extends DummyClass {
		@Override
		public void methodContractHasMethodAccess() {
			if (pre()) {
				assert getValue() == 5;
			}
		}

		@Override
		public void methodContractHasMethodAccessAndMethodAlsoInContract() {
			if (pre()) {
				assert getValueAlsoInContract() == 5;
			}
		}

		@Override
		protected int getValueAlsoInContract() {
			return ignored();
		}
	}

}
