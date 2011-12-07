package next.systemtest;

import next.Contract;

import org.junit.Before;
import org.junit.Test;

import static next.Condition.ignored;
import static next.Condition.pre;

public class MethodCallSystemTest extends TransformerAwareTest {
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
			pre();
			assert getValue() == 5;
		}

		@Override
		public void methodContractHasMethodAccessAndMethodAlsoInContract() {
			pre();
			assert getValueAlsoInContract() == 5;
		}

		@Override
		protected int getValueAlsoInContract() {
			return ignored();
		}
	}

}
