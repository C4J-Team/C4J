package de.vksi.c4j.systemtest.precondition;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PreConditionInConstructorSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPreCondition() {
		new DummyClass(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFails() {
		new DummyClass(0);
	}

	@ContractReference(DummyContract.class)
	public static class DummyClass {
		public DummyClass(int arg) {
		}
	}

	public static class DummyContract extends DummyClass {
		public DummyContract(final int arg) {
			super(arg);
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}
}
