package de.andrena.c4j.systemtest.config.purebehaviorempty;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;

public class PureBehaviorEmptySystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();
	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testPureNotValidated() {
		target.pureMethod();
	}

	@Test
	public void testInvariantNotCalled() {
		ContractClass.invariantCalled = false;
		target.pureMethod();
		assertTrue(ContractClass.invariantCalled);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int value;

		@Pure
		public void pureMethod() {
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
