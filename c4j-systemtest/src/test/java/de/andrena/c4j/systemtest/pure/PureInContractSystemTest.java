package de.andrena.c4j.systemtest.pure;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;

public class PureInContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPureInContractForPureMethod() {
		new TargetClassForPureMethod().method();
	}

	@ContractReference(ContractClassForPureMethod.class)
	public static class TargetClassForPureMethod {
		public void method() {
		}

		@Pure
		public boolean pureMethod() {
			return true;
		}
	}

	public static class ContractClassForPureMethod extends TargetClassForPureMethod {
		@Target
		private TargetClassForPureMethod target;

		@Override
		public void method() {
			if (pre()) {
				assert target.pureMethod();
			}
			if (post()) {
				assert target.pureMethod();
			}
		}
	}

	@Test(expected = AssertionError.class)
	public void testPureInContractForUnpureMethod() {
		new TargetClassForUnpureMethod().method();
	}

	@ContractReference(ContractClassForUnpureMethod.class)
	public static class TargetClassForUnpureMethod {
		public void method() {
		}

		public boolean unpureMethod() {
			return true;
		}
	}

	public static class ContractClassForUnpureMethod extends TargetClassForUnpureMethod {
		@Target
		private TargetClassForUnpureMethod target;

		@Override
		public void method() {
			if (pre()) {
				assert target.unpureMethod();
			}
			if (post()) {
				assert target.unpureMethod();
			}
		}
	}

	@Test
	public void testPureInContractForChangingState() {
		new TargetClassForChangingState().method();
	}

	@ContractReference(ContractClassForChangingState.class)
	public static class TargetClassForChangingState {
		public void method() {
		}
	}

	public static class ContractClassForChangingState extends TargetClassForChangingState {
		private int numCall = 0;

		@Override
		public void method() {
			if (pre()) {
				numCall++;
			}
			if (post()) {
				numCall++;
			}
		}
	}

}
