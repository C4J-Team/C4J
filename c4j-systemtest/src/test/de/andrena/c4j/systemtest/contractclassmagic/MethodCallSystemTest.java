package de.andrena.c4j.systemtest.contractclassmagic;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;

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

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int value;

		public void setValue(int value) {
			this.value = value;
		}

		@Pure
		protected int getValue() {
			return value;
		}

		@Pure
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
		@Target
		private TargetClass target;

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

		@Pure
		public int getFive() {
			return 5;
		}
	}
}
