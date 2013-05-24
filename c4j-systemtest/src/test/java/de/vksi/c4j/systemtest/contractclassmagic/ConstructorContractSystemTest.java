package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ConstructorContract;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ConstructorContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void constructorContractSuccess() {
		new TargetClass(1);
	}

	@Test(expected = AssertionError.class)
	public void constructorContractFailure() {
		new TargetClass(0);
	}

	@Test(expected = UsageError.class)
	public void constructorContract_NoArgConstructorMissing() throws Exception {
		new TargetForContractClassMissingNoArgConstructor().method(1);
	}

	@Test(expected = UsageError.class)
	public void constructorContract_NonStaticNestedContractClass() throws Exception {
		new TargetForNonStaticNestedContractClass().method(1);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		public TargetClass(int value) {
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass {
		@ConstructorContract
		public void construct(int value) {
			if (preCondition()) {
				assert value > 0;
			}
		}
	}

	@ContractReference(ContractClassMissingNoArgConstructor.class)
	private static class TargetForContractClassMissingNoArgConstructor {
		public void method(int arg) {
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClassMissingNoArgConstructor extends TargetForContractClassMissingNoArgConstructor {
		public ContractClassMissingNoArgConstructor(int arg) {
		}

		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@ContractReference(NonStaticNestedContractClass.class)
	private static class TargetForNonStaticNestedContractClass {
		public void method(int arg) {
		}
	}

	private class NonStaticNestedContractClass extends TargetForNonStaticNestedContractClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

}
