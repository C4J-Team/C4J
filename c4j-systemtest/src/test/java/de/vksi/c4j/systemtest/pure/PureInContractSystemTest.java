package de.vksi.c4j.systemtest.pure;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
			if (preCondition()) {
				assert target.pureMethod();
			}
			if (postCondition()) {
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
			if (preCondition()) {
				assert target.unpureMethod();
			}
			if (postCondition()) {
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
			if (preCondition()) {
				numCall++;
			}
			if (postCondition()) {
				numCall++;
			}
		}
	}

}
