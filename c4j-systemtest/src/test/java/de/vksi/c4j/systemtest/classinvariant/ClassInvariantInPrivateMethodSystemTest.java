package de.vksi.c4j.systemtest.classinvariant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ClassInvariantInPrivateMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testClassInvariantInPrivateMethod() {
		new TargetClass();
		assertThat(ContractClass.invariantCalled, is(true));
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		private int value = 0;

		public TargetClass() {
			init();
			value = 1;
		}

		private void init() {
			value = 0;
		}

		@Pure
		public int getValue() {
			return value;
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass {
		private static boolean invariantCalled;

		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			invariantCalled = true;
			assert target.getValue() > 0;
		}
	}

}
