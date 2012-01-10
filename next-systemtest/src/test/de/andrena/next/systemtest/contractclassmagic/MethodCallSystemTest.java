package de.andrena.next.systemtest.contractclassmagic;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class MethodCallSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testPreConditionWithMethodAccess() {
		target.setValue(5);
		target.methodContractHasMethodAccess();
	}

	@Test
	public void testPreConditionWithMethodAccessAndMethodAlsoInContract() {
		target.setValue(5);
		target.methodContractHasMethodAccessAndMethodAlsoInContract();
	}

	@Test
	public void testPreConditionWithMethodAccessOnContractOnly() {
		target.setValue(5);
		target.methodContractHasMethodAccessOnContractOnly();
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
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

		public void methodContractHasMethodAccessOnContractOnly() {
		}
	}

	public static class ContractClass extends TargetClass {
		TargetClass target = Condition.target();

		@Override
		public void methodContractHasMethodAccess() {
			if (pre()) {
				assert target.getValue() == 5;
			}
		}

		@Override
		public void methodContractHasMethodAccessAndMethodAlsoInContract() {
			if (pre()) {
				assert target.getValueAlsoInContract() == 5;
			}
		}

		@Override
		protected int getValueAlsoInContract() {
			return ignored();
		}

		@Override
		public void methodContractHasMethodAccessOnContractOnly() {
			if (pre()) {
				assert target.getValue() == getFive();
			}
		}

		public int getFive() {
			return 5;
		}
	}
}
