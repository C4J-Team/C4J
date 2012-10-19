package de.vksi.c4j.systemtest.lsp;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Test;

import de.vksi.c4j.ContractReference;

public class MultipleInheritanceSystemTest {

	@Test
	public void multipleInheritance_SameResults_Valid() throws Exception {
		new TargetClass().method(2);
	}

	@Test(expected = AssertionError.class)
	public void multipleInheritance_SameResults_Invalid() throws Exception {
		new TargetClass().method(0);
	}

	//	@Test(expected = UsageError.class)
	//	public void multipleInheritance_DifferentResults() throws Exception {
	//		new TargetClass().method(1);
	//	}

	public static class TargetClass implements FirstInterface, SecondInterface {
		@Override
		public void method(int arg) {
		}
	}

	@ContractReference(FirstContract.class)
	public interface FirstInterface {
		void method(int arg);
	}

	public static class FirstContract implements FirstInterface {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@ContractReference(SecondContract.class)
	public interface SecondInterface {
		void method(int arg);
	}

	public static class SecondContract implements SecondInterface {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 1;
			}
		}
	}
}
