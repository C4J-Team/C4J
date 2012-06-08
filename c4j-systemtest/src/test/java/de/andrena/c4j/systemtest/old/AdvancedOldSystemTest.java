package de.andrena.c4j.systemtest.old;

import static de.andrena.c4j.Condition.old;
import static de.andrena.c4j.Condition.postCondition;
import static org.junit.Assert.fail;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.UsageError;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class AdvancedOldSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testValidCalls() {
		new TargetClassValid().method("abc", 123);
	}

	@Test
	public void testInvalidReferenceOnLocalVariable() {
		transformerAware.expectGlobalLog(Level.ERROR, "Illegal access on local variable within old().");
		try {
			new TargetClassInvalidLocalVariable().invalidMethod("abc", 123);
			fail("expected " + UsageError.class.getName());
		} catch (UsageError e) {
		}
	}

	@ContractReference(ContractClassValid.class)
	public static class TargetClassValid {
		public void method(String name, int value) {
		}

		@Pure
		public String getName() {
			return "abc";
		}
	}

	public static class ContractClassValid extends TargetClassValid {
		@Target
		private TargetClassValid target;

		private OtherClass other = new OtherClass();

		@Override
		public void method(String name, int value) {
			if (postCondition()) {
				assert target.getName().equals(old(target.getName()));
				assert 3 == old(3);
				assert other.otherMethod(0.01) == old(other.otherMethod(0.01));
			}
		}

	}

	public static class OtherClass {
		@Pure
		public double otherMethod(double value) {
			return value;
		}
	}

	@ContractReference(ContractClassInvalidLocalVariable.class)
	public static class TargetClassInvalidLocalVariable {
		public void invalidMethod(String name, int value) {
		}
	}

	public static class ContractClassInvalidLocalVariable extends TargetClassInvalidLocalVariable {
		@Override
		public void invalidMethod(String name, int value) {
			if (postCondition()) {
				double localVar = 3.0;
				assert 3.0 == old(localVar);
			}
		}
	}
}
