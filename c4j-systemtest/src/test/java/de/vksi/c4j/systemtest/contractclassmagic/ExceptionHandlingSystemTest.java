package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ExceptionHandlingSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testAbortOnPreconditionFailure() {
		TargetClass target = new TargetClass();
		target.value = 1;
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("method (pre-condition)");
		target.method(0);
	}

	@Test
	public void testAbortOnNestedContractFailure() {
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("nested (pre-condition)");
		new TargetClass().method(1);
	}

	@Test
	public void testAbortOnPostconditionFailure() {
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("method (post-condition)");
		new TargetClass().method(2);
	}

	@Test
	public void testAbortOnNestedException() {
		expectedException.expect(RuntimeException.class);
		new TargetClass().method(3);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public int value;

		public int method(int arg) {
			value = 1;
			nestedMethod(arg);
			return arg;
		}

		// prevents execution of class-invariant in nested method
		@Pure
		public void nestedMethod(int arg) {
			if (arg == 3) {
				throw new RuntimeException();
			}
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert target.value == 0;
		}

		@Override
		public int method(int arg) {
			if (preCondition()) {
				assert arg > 0 : "method";
			}
			if (postCondition()) {
				assert arg > 2 : "method";
			}
			return (Integer) ignored();
		}

		@Override
		public void nestedMethod(int arg) {
			if (preCondition()) {
				assert arg > 1 : "nested";
			}
		}
	}
}
