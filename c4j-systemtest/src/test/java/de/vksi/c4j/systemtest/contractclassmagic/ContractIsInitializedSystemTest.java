package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractIsInitializedSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void contractIsInitialized() {
		new TargetClass().method();
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public void method() {
		}
	}

	private static class ContractClass extends TargetClass {
		private String value = "sampleValue";

		@Override
		public void method() {
			if (preCondition()) {
				assert "sampleValue".equals(value);
			}
		}
	}

	@Test
	public void multipleContractsAreInitialized() {
		new TargetClass1().method();
	}

	@ContractReference(ContractClass1.class)
	private static class TargetClass1 extends TargetClass2 {
		public void method() {
		}
	}

	private static class ContractClass1 extends TargetClass1 {
		private String value = "sampleValue";

		@Override
		public void method() {
			if (preCondition()) {
				assert "sampleValue".equals(value);
			}
		}
	}

	@ContractReference(ContractClass2.class)
	private static class TargetClass2 {
	}

	private static class ContractClass2 extends TargetClass2 {
		public ContractClass2() {
			if (preCondition()) {
			}
		}
	}

	@Test
	public void contractClassIsInitialized() throws Exception {
		new StaticTargetClass().method();
	}

	@ContractReference(StaticContractClass.class)
	private static class StaticTargetClass {
		public void method() {
		}
	}

	private static class StaticContractClass extends StaticTargetClass {
		private static String value = "sampleValue";

		@Override
		public void method() {
			if (preCondition()) {
				assert "sampleValue".equals(value);
			}
		}
	}
}
