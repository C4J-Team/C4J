package de.andrena.c4j.systemtest.old;

import static de.andrena.c4j.Condition.old;
import static de.andrena.c4j.Condition.postCondition;
import static org.junit.Assert.fail;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.internal.ContractError;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class AdvancedOldSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();
	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testValidCalls() {
		target.method("abc", 123);
	}

	@Test
	public void testInvalidReferenceOnLocalVariable() {
		transformerAware.expectGlobalLog(Level.ERROR, "Illegal access on local variable within old().");
		try {
			target.invalidMethod("abc", 123);
			fail("expected " + ContractError.class.getName());
		} catch (ContractError e) {
		}
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(String name, int value) {
		}

		public void invalidMethod(String name, int value) {
		}

		@Pure
		public String getName() {
			return "abc";
		}
	}

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		private OtherClass other = new OtherClass();

		@Override
		public void method(String name, int value) {
			if (postCondition()) {
				assert target.getName().equals(old(target.getName()));
				assert 3 == old(3);
				assert other.otherMethod(0.01) == old(other.otherMethod(0.01));
			}
		}

		@Override
		public void invalidMethod(String name, int value) {
			if (postCondition()) {
				double localVar = 3.0;
				assert 3.0 == old(localVar);
			}
		}
	}

	public static class OtherClass {
		@Pure
		public double otherMethod(double value) {
			return value;
		}
	}
}
