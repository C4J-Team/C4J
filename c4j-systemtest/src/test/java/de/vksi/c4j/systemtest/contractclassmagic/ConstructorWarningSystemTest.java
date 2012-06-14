package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ConstructorWarningSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void test() {
		transformerAware.banGlobalLog(Level.WARN, "could not find a matching constructor in affected class "
				+ TargetClass.class.getName() + " for constructor "
				+ ContractClass.class.getName() + "()");
		new TargetClass(1);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public TargetClass(int arg) {
		}
	}

	public static class ContractClass extends TargetClass {
		public ContractClass(int arg) {
			super(arg);
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

}
