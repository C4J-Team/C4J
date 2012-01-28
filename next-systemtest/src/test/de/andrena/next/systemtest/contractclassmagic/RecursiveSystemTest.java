package de.andrena.next.systemtest.contractclassmagic;

import static de.andrena.next.Condition.post;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class RecursiveSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testCorrectEquals() {
		new TargetClassWithCorrectEquals().equals(new TargetClassWithCorrectEquals());
	}

	@Contract(ContractClassWithCorrectEquals.class)
	public static class TargetClassWithCorrectEquals {
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
	}

	public static class ContractClassWithCorrectEquals extends TargetClassWithCorrectEquals {
		private TargetClassWithCorrectEquals target = Condition.target();

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
		private TargetClassWithUnreflexiveEquals target = Condition.target();

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
		private TargetClassWithUnsymmetricEquals target = Condition.target();

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
