package de.vksi.c4j.systemtest.config.purebehaviorempty;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
	private static class TargetClass {
		protected int value;

		@Pure
		public void pureMethod() {
			value = 3;
		}
	}

	private static class ContractClass extends TargetClass {
		public static boolean invariantCalled;

		@ClassInvariant
		public void invariant() {
			invariantCalled = true;
		}
	}
}
