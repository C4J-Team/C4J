package de.andrena.c4j.systemtest.config.purebehaviorskiponly;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.ClassInvariant;
import de.andrena.next.Contract;
import de.andrena.next.Pure;
import de.andrena.next.PureTarget;

public class PureBehaviorSkipOnlySystemTest {
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
		assertFalse(ContractClass.invariantCalled);
	}

	@Contract(ContractClass.class)
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

	@Test
	public void testInvariantNotCalledWhenPureInContract() {
		TargetClassWithPureInContract target = new TargetClassWithPureInContract();
		ContractClassWithPureInContract.invariantCalled = false;
		target.pureMethod();
		assertFalse(ContractClassWithPureInContract.invariantCalled);
	}

	@Contract(ContractClassWithPureInContract.class)
	public static class TargetClassWithPureInContract {
		protected int value;

		public void pureMethod() {
			value = 3;
		}
	}

	public static class ContractClassWithPureInContract extends TargetClassWithPureInContract {
		public static boolean invariantCalled;

		@ClassInvariant
		public void invariant() {
			invariantCalled = true;
		}

		@Override
		@PureTarget
		public void pureMethod() {
		}
	}
}
