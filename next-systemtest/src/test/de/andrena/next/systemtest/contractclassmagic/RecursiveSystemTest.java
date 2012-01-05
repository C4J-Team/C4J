package de.andrena.next.systemtest.contractclassmagic;

import static de.andrena.next.Condition.post;

import org.junit.Ignore;
import org.junit.Test;

import de.andrena.next.Contract;

public class RecursiveSystemTest {

	@Test
	@Ignore("not working as expected")
	public void testCorrectEquals() {
		new TargetClassWithCorrectEquals().equals(new TargetClassWithCorrectEquals());
	}

	@Test(expected = AssertionError.class)
	public void testUnreflexiveEquals() {
		TargetClassWithUnreflexiveEquals target = new TargetClassWithUnreflexiveEquals();
		target.equals(target);
	}

	@Test(expected = AssertionError.class)
	public void testUnsymmetricEquals() {
		new TargetClassWithUnsymmetricEquals(3).equals(new TargetClassWithUnsymmetricEquals(4));
	}

	@Contract(ContractClass.class)
	public static class TargetClassWithCorrectEquals {
		@Override
		public boolean equals(Object obj) {
			System.out.println(System.identityHashCode(this) + " " + System.identityHashCode(obj));
			return this == obj;
		}
	}

	@Contract(ContractClass.class)
	public static class TargetClassWithUnreflexiveEquals {
		@Override
		public boolean equals(Object obj) {
			return this != obj;
		}
	}

	@Contract(ContractClass.class)
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

	public static class ContractClass {
		@Override
		public boolean equals(Object obj) {
			if (post()) {
				System.out.println("just before is reflexive");
				assert this.equals(this) : "is reflexive";
				assert this.equals(obj) == obj.equals(this) : "is symmetric";
			}
			return false;
		}
	}

}
