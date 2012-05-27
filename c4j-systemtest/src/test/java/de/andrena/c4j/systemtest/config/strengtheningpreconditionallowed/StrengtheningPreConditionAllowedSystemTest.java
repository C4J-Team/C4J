package de.andrena.c4j.systemtest.config.strengtheningpreconditionallowed;

import static de.andrena.c4j.Condition.pre;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class StrengtheningPreConditionAllowedSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		transformerAwareRule.banGlobalLog(Level.ERROR, "found strengthening pre-condition in "
						+ ContractClass.class.getName() + ".method(int)" + " which is already defined from" + " "
						+ SuperClassContract.class.getName() + " - ignoring the pre-condition");
		new TargetClass().method(0);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass extends SuperClass {
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}

	@ContractReference(SuperClassContract.class)
	public static class SuperClass {
		public void method(int arg) {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > -1;
			}
		}
	}
}