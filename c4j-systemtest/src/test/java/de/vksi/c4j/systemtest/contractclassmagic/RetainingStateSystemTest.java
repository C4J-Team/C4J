package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(int expectedNumCall) {
		}
	}

	public static class ContractClass extends TargetClass {
		private int numCall;

		@Override
		public void method(final int expectedNumCall) {
			if (preCondition()) {
				assert expectedNumCall == numCall;
				numCall++;
			}
		}
	}
}
