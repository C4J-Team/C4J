package de.vksi.c4j.systemtest.classinvariant;

import static de.vksi.c4j.Condition.unchanged;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.MutableString;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class UnchangedInClassInvariantSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testUnchangedInClassInvariant() {
		new TargetClass().leavePrimitiveValueUnchanged();
	}

	@Test(expected = AssertionError.class)
	public void testUnchangedInClassInvariantFailing() {
		new TargetClass().changePrimitiveValue();
	}

	@Test
	public void testUnchangedInClassInvariantForReference() {
		new TargetClass().leaveReferenceValueUnchanged();
	}

	@Test(expected = AssertionError.class)
	public void testUnchangedInClassInvariantForReferenceFailing() {
		new TargetClass().changeReferenceValue();
	}

	@Test(expected = AssertionError.class)
	public void testUnchangedInClassInvariantForReferenceReassign() {
		new TargetClass().reassignReferenceValue();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected int primitiveValue;
		protected MutableString referenceValue;

		public TargetClass() {
			primitiveValue = 1;
			referenceValue = new MutableString("constructor");
		}

		public void leavePrimitiveValueUnchanged() {
		}

		public void changePrimitiveValue() {
			primitiveValue--;
		}

		public void leaveReferenceValueUnchanged() {
		}

		public void changeReferenceValue() {
			referenceValue.setValue("new value");
		}

		public void reassignReferenceValue() {
			referenceValue = new MutableString("new value");
		}
	}

	public static class ContractClass {
		@Target
		private TargetClass target;

		@ClassInvariant
		public void invariant() {
			assert unchanged(target.primitiveValue);
			assert unchanged(target.referenceValue);
		}

	}
}
