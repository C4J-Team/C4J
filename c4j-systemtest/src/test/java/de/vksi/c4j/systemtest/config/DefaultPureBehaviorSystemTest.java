package de.vksi.c4j.systemtest.config;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class DefaultPureBehaviorSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();
	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test(expected = AssertionError.class)
	public void testPureValidated() {
		target.pureMethodFailing();
	}

	@Test
	public void testInvariantNotCalled() {
		ContractClass.invariantCalled = false;
		target.pureMethod();
		assertFalse(ContractClass.invariantCalled);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int value;

		@Pure
		public void pureMethod() {
		}

		@Pure
		public void pureMethodFailing() {
			value = 3;
		}
	}

	public static class ContractClass extends TargetClass {
		public static boolean invariantCalled;

		@ClassInvariant
		public void invariant() {
			invariantCalled = true;
		}
	}
}
