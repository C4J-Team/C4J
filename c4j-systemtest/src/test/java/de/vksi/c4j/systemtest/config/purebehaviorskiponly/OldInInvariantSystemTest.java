package de.vksi.c4j.systemtest.config.purebehaviorskiponly;

import static de.vksi.c4j.Condition.old;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldInInvariantSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();
	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testOldInInvariant() {
		target.method();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method() {
		}

		public int getOne() {
			return 1;
		}
	}

	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert old(target.getOne()) == 1;
		}
	}
}
