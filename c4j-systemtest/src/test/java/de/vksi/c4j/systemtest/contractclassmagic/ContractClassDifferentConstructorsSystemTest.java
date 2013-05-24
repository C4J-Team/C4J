package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractClassDifferentConstructorsSystemTest {

	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testMatchingConstructor() {
		new TargetClass("abc").check();
	}

	@Test
	public void testNonMatchingConstructor() {
		new TargetClass(54).check();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public TargetClass(String arg) {
		}

		public TargetClass(Integer arg) {
		}

		public void check() {
		}
	}

	public static class ContractClass extends TargetClass {
		private boolean initialized = true;

		public ContractClass() {
			super("");
		}

		@Override
		public void check() {
			if (preCondition()) {
				assert initialized;
			}
		}
	}
}
