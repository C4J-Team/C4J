package de.vksi.c4j.systemtest.hamcrestintegration;

import static de.vksi.c4j.Condition.preCondition;
import static org.hamcrest.JavaLangMatcherAssert.that;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class HamcrestIntegrationSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testHamcrestIntegrationWithSuccess() {
		new DummyClass().method("Dummy call");
	}

	@Test(expected = AssertionError.class)
	public void testHamcrestIntegrationWithFailure() {
		new DummyClass().method("ABC");
	}

	@ContractReference(DummyContract.class)
	private static class DummyClass {
		public void method(String arg) {
		}
	}

	private static class DummyContract extends DummyClass {
		@Override
		public void method(String arg) {
			if (preCondition()) {
				assert that(arg, startsWith("Dummy"));
			}
		}
	}
}
