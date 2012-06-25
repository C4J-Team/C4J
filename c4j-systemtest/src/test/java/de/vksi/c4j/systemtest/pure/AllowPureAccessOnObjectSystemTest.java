package de.vksi.c4j.systemtest.pure;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.MutableString;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class AllowPureAccessOnObjectSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testAllowPureAccessFromTargetClass() {
		new TargetClass().modifyValue();
	}

	@Test
	public void testAllowPureAccessFromContractClass() {
		new TargetClass().modifyValueInContract();
	}

	@Test
	public void testAllowPureReassignmentFromTargetClass() {
		new TargetClass().reassignValue();
	}

	@Test
	public void testAllowPureReassignmentFromContractClass() {
		new TargetClass().reassignValueInContract();
	}

	@ContractReference(TargetClassContract.class)
	public static class TargetClass {
		@AllowPureAccess
		private MutableString value = new MutableString();

		@AllowPureAccess
		public static MutableString staticValue = new MutableString();

		@Pure
		public void modifyValue() {
			value.setValue("new value");
		}

		@Pure
		public void modifyValueInContract() {
		}

		@Pure
		public void reassignValue() {
			value = new MutableString("new value");
		}

		@Pure
		public void reassignValueInContract() {
		}
	}

	public static class TargetClassContract extends TargetClass {
		@Override
		public void modifyValueInContract() {
			if (preCondition()) {
				TargetClass.staticValue.setValue("contract value");
			}
		}

		@Override
		public void reassignValueInContract() {
			if (preCondition()) {
				TargetClass.staticValue = new MutableString("contract value");
			}
		}
	}
}
