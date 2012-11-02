package de.vksi.c4j.systemtest.lsp;

import static de.vksi.c4j.Condition.preCondition;
import static org.hamcrest.Matchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class MultipleInheritanceSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void multipleInheritance_SameResults_Valid() throws Exception {
		new TargetClass().method(2);
	}

	@Test
	public void multipleInheritance_SameResults_Invalid() throws Exception {
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage(containsString("(pre-condition)"));
		new TargetClass().method(0);
	}

	@Test
	public void multipleInheritance_DifferentResults() throws Exception {
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage(containsString("Invalid multiple inheritance"));
		new TargetClass().method(1);
	}

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
