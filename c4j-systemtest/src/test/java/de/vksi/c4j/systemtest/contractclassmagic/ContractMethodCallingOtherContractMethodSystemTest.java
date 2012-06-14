package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static de.vksi.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractMethodCallingOtherContractMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = UsageError.class)
	public void testErrorWhenCallingOtherTargetMethod() {
		new TargetClassCallingOtherTargetMethod().method();
	}

	@Test(expected = UsageError.class)
	public void testErrorWhenCallingOverriddenContractMethod() {
		new TargetClassCallingOverriddenContractMethod().method();
	}

	@Test
	public void testSuccessWhenCallingOtherContractMethod() {
		new TargetClassCallingOtherContractMethod().method();
	}

	@ContractReference(ContractClassCallingOtherTargetMethod.class)
	public static class TargetClassCallingOtherTargetMethod {
		public void method() {
		}

		public int getX() {
			return 0;
		}
	}

	public static class ContractClassCallingOtherTargetMethod extends TargetClassCallingOtherTargetMethod {
		@Override
		public void method() {
			if (preCondition()) {
				assert getX() == 0;
			}
		}
	}

	@ContractReference(ContractClassCallingOtherContractMethod.class)
	public static class TargetClassCallingOtherContractMethod {
		public void method() {
		}
	}

	public static class ContractClassCallingOtherContractMethod extends TargetClassCallingOtherContractMethod {
		@Override
		public void method() {
			if (preCondition()) {
				assert otherContractMethod() == 0;
			}
		}

		private int otherContractMethod() {
			return 0;
		}
	}

	@ContractReference(ContractClassCallingOverriddenContractMethod.class)
	public static class TargetClassCallingOverriddenContractMethod {
		public void method() {
		}

		public int otherMethod() {
			return 0;
		}
	}

	public static class ContractClassCallingOverriddenContractMethod extends TargetClassCallingOverriddenContractMethod {
		@Override
		public void method() {
			if (preCondition()) {
				assert otherMethod() == 0;
			}
		}

		@Override
		public int otherMethod() {
			if (postCondition()) {
				int result = (Integer) result();
				assert result == 0;
			}
			return (Integer) ignored();
		}
	}
}
