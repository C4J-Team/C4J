package de.andrena.c4j.systemtest.contractclassmagic;

import static de.andrena.c4j.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Contract;

public class RetainingStateSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testRetainingState() {
		target.method(0);
		target.method(1);
		target.method(2);
		target.method(3);
	}

	@Test(expected = AssertionError.class)
	public void testRetainingStateFailingOnFirst() {
		target.method(1);
	}

	@Test(expected = AssertionError.class)
	public void testRetainingStateFailingOnSecond() {
		target.method(0);
		target.method(0);
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		public void method(int expectedNumCall) {
		}
	}

	public static class ContractClass extends TargetClass {
		private int numCall;

		@Override
		public void method(final int expectedNumCall) {
			if (pre()) {
				assert expectedNumCall == numCall;
				numCall++;
			}
		}
	}
}
