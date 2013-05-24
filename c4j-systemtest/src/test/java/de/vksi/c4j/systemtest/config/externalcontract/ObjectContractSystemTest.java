package de.vksi.c4j.systemtest.config.externalcontract;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.result;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Contract;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ObjectContractSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testHashCodeConsistency() {
		TargetClassViolatingHashCodeConsistency target1 = new TargetClassViolatingHashCodeConsistency(1);
		TargetClassViolatingHashCodeConsistency target2 = new TargetClassViolatingHashCodeConsistency(1);
		target1.equals(target2);
	}

	private static class TargetClassViolatingHashCodeConsistency {
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

	@SuppressWarnings("unused")
	@Contract(forTarget = Object.class)
	private static class ObjectContract {
		@Target
		private Object target;

		@Override
		public boolean equals(Object obj) {
			if (postCondition()) {
				Boolean result = result();
				if (result) {
					assert target.hashCode() == obj.hashCode();
				}
			}
			return (Boolean) ignored();
		}
	}
}
