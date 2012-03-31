package de.andrena.c4j.systemtest.inheritance;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.ContractReference;

public class SeparateContractInstantiationSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testSeparateContractInstantiation() {
		new SubClass().interfaceMethod();
	}

	@ContractReference(InterfaceContract.class)
	public interface Interface {
		public String interfaceMethod();
	}

	public static class InterfaceContract implements Interface {
		private String fixedInstance;

		@Override
		public String interfaceMethod() {
			if (post()) {
				String result = result(String.class);
				if (fixedInstance == null) {
					fixedInstance = result;
				}
				assert fixedInstance.equals(result);
			}
			return null;
		}
	}

	public static class SubClass extends SuperClass {
		@Override
		public String interfaceMethod() {
			super.interfaceMethod();
			return "SubClass";
		}
	}

	public static class SuperClass implements Interface {
		@Override
		public String interfaceMethod() {
			return "SuperClass";
		}
	}
}
