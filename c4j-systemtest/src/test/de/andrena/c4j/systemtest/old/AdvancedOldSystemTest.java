package de.andrena.c4j.systemtest.old;

import static de.andrena.c4j.Condition.old;
import static de.andrena.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class AdvancedOldSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void test() {
		TargetClass target = new TargetClass();
		target.method("abc", 123);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void method(String name, int value) {
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
	}

	public static class OtherClass {
		@Pure
		public double otherMethod(double value) {
			return value;
		}
	}
}
