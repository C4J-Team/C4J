package de.vksi.c4j.systemtest.old;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldWithConstructorSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testOldWithConstructor() {
		new DummyClass().method();
	}

	@ContractReference(DummyContract.class)
	private static class DummyClass {
		public void method() {
		}
	}

	private static class DummyContract extends DummyClass {
		@Override
		public void method() {
			if (postCondition()) {
				assert old(new Integer(3)).equals(Integer.valueOf(3));
			}
		}
	}
}
