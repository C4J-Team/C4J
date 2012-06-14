package com.external;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.post;
import static de.vksi.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Condition;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testMA() {
		A a = new A();
		a.mA(4, 6);
	}

	@ContractReference(AContract.class)
	public static class A {
		private int z;

		public int mA(int x, int y) {
			z = x + y;
			return z;
		}

		@Pure
		public int getZ() {
			return z;
		}

	}

	public static class AContract extends A {
		@Override
		public int mA(int x, int y) {
			if (pre()) {
				assert x > 3 : "x > 3";
				assert y > 4 : "y > 4";
			}
			if (post()) {
				Integer result = Condition.result(Integer.class);
				assert result.intValue() == (x + y) : "result == x + y";
			}
			return (Integer) ignored();
		}
	}
}
