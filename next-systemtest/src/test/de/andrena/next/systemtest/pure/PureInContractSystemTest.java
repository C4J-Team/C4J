package de.andrena.next.systemtest.pure;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Contract;
import de.andrena.next.Pure;
import de.andrena.next.systemtest.TransformerAwareRule;

public class PureInContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPureInContractForPureMethod() {
		new TargetClassForPureMethod().method();
	}

	@Contract(ContractClassForPureMethod.class)
	public static class TargetClassForPureMethod {
		public void method() {
		}

		@Pure
		public boolean pureMethod() {
			return true;
		}
	}

	public static class ContractClassForPureMethod extends TargetClassForPureMethod {
		private TargetClassForPureMethod target = Condition.target();

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
		new TargetClassForPureMethod().method();
	}

	@Contract(ContractClassForPureMethod.class)
	public static class TargetClassForUnpureMethod {
		public void method() {
		}

		public boolean unpureMethod() {
			return true;
		}
	}

	public static class ContractClassForUnpureMethod extends TargetClassForUnpureMethod {
		private TargetClassForUnpureMethod target = Condition.target();

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

	@Contract(ContractClassForChangingState.class)
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
