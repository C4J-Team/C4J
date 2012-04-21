package de.andrena.c4j.systemtest.config.externalcontract;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.Target;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ObjectContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testHashCodeConsistency() {
		TargetClassViolatingHashCodeConsistency target1 = new TargetClassViolatingHashCodeConsistency(1);
		TargetClassViolatingHashCodeConsistency target2 = new TargetClassViolatingHashCodeConsistency(1);
		target1.equals(target2);
	}

	public static class TargetClassViolatingHashCodeConsistency {
		private int x;

		public TargetClassViolatingHashCodeConsistency(int x) {
			this.x = x;
		}

		@Override
		public boolean equals(Object obj) {
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			TargetClassViolatingHashCodeConsistency other = (TargetClassViolatingHashCodeConsistency) obj;
			return x == other.x;
		}
	}

	public static class ObjectContract {
		@Target
		private Object target;

		@Override
		public boolean equals(Object obj) {
			if (post()) {
				boolean result = result();
				if (result) {
					assert target.hashCode() == obj.hashCode();
				}
			}
			return ignored();
		}
	}
}
