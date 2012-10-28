package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class StaticMemberAccessSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testStaticFieldAccessFromContractClass() throws Exception {
		new TargetClass().method();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public static final Boolean STATIC_VAR = Boolean.TRUE;

		public void method() {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method() {
			if (preCondition()) {
				assert Boolean.TRUE.equals(STATIC_VAR);
			}
		}
	}

	@Test
	public void testOverriddenStaticFieldAccessFromContractClass() throws Exception {
		new TargetClassOverridden().method();
	}

	@ContractReference(ContractClassOverridden.class)
	public static class TargetClassOverridden {
		public static final Boolean STATIC_VAR = Boolean.TRUE;

		public void method() {
		}
	}

	public static class ContractClassOverridden extends TargetClassOverridden {
		private static final Boolean STATIC_VAR = Boolean.FALSE;

		@Override
		public void method() {
			if (preCondition()) {
				assert Boolean.FALSE.equals(STATIC_VAR);
			}
		}
	}

	@Test
	public void testStaticMethodAccessFromContractClass() throws Exception {
		new TargetClassMethod().method();
	}

	@ContractReference(ContractClassMethod.class)
	public static class TargetClassMethod {
		public void method() {
		}

		@Pure
		public static String staticMethod() {
			return "static value";
		}
	}

	public static class ContractClassMethod extends TargetClassMethod {
		@Override
		public void method() {
			if (preCondition()) {
				assert "static value".equals(staticMethod());
			}
		}
	}
}
