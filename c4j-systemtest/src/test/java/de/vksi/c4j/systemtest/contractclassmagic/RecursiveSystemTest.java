package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.postCondition;
import static org.hamcrest.Matchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class RecursiveSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testCorrectEquals() {
		new TargetClassWithCorrectEquals().equals(new TargetClassWithCorrectEquals());
	}

	@ContractReference(ContractClassWithCorrectEquals.class)
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
			if (postCondition()) {
				assert target.equals(target) : "is reflexive";
				assert target.equals(obj) == obj.equals(target) : "is symmetric";
			}
			return false;
		}
	}

	@Test
	public void testUnreflexiveEquals() {
		TargetClassWithUnreflexiveEquals target = new TargetClassWithUnreflexiveEquals();
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage(containsString("is reflexive"));
		target.equals(target);
	}

	@ContractReference(ContractClassWithUnreflexiveEquals.class)
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
			if (postCondition()) {
				assert target.equals(target) : "is reflexive";
				assert target.equals(obj) == obj.equals(target) : "is symmetric";
			}
			return false;
		}
	}

	@Test
	public void testUnsymmetricEquals() {
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage(containsString("is symmetric"));
		new TargetClassWithUnsymmetricEquals(3).equals(new TargetClassWithUnsymmetricEquals(4));
	}

	@ContractReference(ContractClassWithUnsymmetricEquals.class)
	public static class TargetClassWithUnsymmetricEquals {
		private int value;

		public TargetClassWithUnsymmetricEquals(int value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TargetClassWithUnsymmetricEquals)) {
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
			if (postCondition()) {
				assert target.equals(target) : "is reflexive";
				assert target.equals(obj) == obj.equals(target) : "is symmetric";
			}
			return false;
		}
	}

}
