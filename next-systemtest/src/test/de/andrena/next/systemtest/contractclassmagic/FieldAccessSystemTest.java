package de.andrena.next.systemtest.contractclassmagic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition.PreCondition;
import de.andrena.next.Contract;
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
		@Override
		public void methodContractHasFieldAccess() {
			new PreCondition() {
				{
					assert value == 5;
				}
			};
		}
	}
}
