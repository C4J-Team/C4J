package de.andrena.c4j.systemtest.contractclassmagic;

import static de.andrena.c4j.Condition.post;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Contract;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;

public class RecursiveSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testCorrectEquals() {
		new TargetClassWithCorrectEquals().equals(new TargetClassWithCorrectEquals());
	}

	@Contract(ContractClassWithCorrectEquals.class)
	public static class TargetClassWithCorrectEquals {
		@Pure
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
	}

	public static class ContractClassWithCorrectEquals extends TargetClassWithCorrectEquals {
		@Target
		private TargetClassWithCorrectEquals target;

		@Override
		public boolean equals(Object obj) {
			if (post()) {
				assert target.equals(target) : "is reflexive";
				assert target.equals(obj) == obj.equals(target) : "is symmetric";
			}
			return false;
		}
	}

	@Test(expected = AssertionError.class)
	public void testUnreflexiveEquals() {
		TargetClassWithUnreflexiveEquals target = new TargetClassWithUnreflexiveEquals();
		target.equals(target);
	}

	@Contract(ContractClassWithUnreflexiveEquals.class)
	public static class TargetClassWithUnreflexiveEquals {
		@Override
		public boolean equals(Object obj) {
			return this != obj;
		}
	}

	public static class ContractClassWithUnreflexiveEquals extends TargetClassWithUnreflexiveEquals {
		@Target
		private TargetClassWithUnreflexiveEquals target;

		@Override
		public boolean equals(Object obj) {
			if (post()) {
				assert target.equals(target) : "is reflexive";
				assert target.equals(obj) == obj.equals(target) : "is symmetric";
			}
			return false;
		}
	}

	@Test(expected = AssertionError.class)
	public void testUnsymmetricEquals() {
		new TargetClassWithUnsymmetricEquals(3).equals(new TargetClassWithUnsymmetricEquals(4));
	}

	@Contract(ContractClassWithUnsymmetricEquals.class)
	public static class TargetClassWithUnsymmetricEquals {
		private int value;

		public TargetClassWithUnsymmetricEquals(int value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TargetClassWithUnreflexiveEquals)) {
				return false;
			}
			return this.value >= ((TargetClassWithUnsymmetricEquals) obj).value;
		}
	}

	public static class ContractClassWithUnsymmetricEquals extends TargetClassWithUnsymmetricEquals {
		@Target
		private TargetClassWithUnsymmetricEquals target;

		public ContractClassWithUnsymmetricEquals(int value) {
			super(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (post()) {
				assert target.equals(target) : "is reflexive";
				assert target.equals(obj) == obj.equals(target) : "is symmetric";
			}
			return false;
		}
	}

}
