package de.vksi.c4j.systemtest.precondition;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PreConditionSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testPreCondition() {
		dummy.method(3);
	}

	@Test(expected = AssertionError.class)
	public void testPreConditionFails() {
		dummy.method(0);
	}

	@ContractReference(DummyContract.class)
	private static class DummyClass {
		public void method(int arg) {
		}
	}

	private static class DummyContract extends DummyClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}
}
