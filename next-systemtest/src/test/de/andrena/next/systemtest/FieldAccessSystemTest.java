package de.andrena.next.systemtest;

import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Contract;

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
			if (pre()) {
				assert value == 5;
			}
		}
	}

}
