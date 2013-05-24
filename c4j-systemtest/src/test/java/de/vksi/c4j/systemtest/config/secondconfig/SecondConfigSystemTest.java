package de.vksi.c4j.systemtest.config.secondconfig;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class SecondConfigSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testContractsDirectory() {
		new TargetClass().method();
	}

	private static class TargetClass {
		@Pure
		public int method() {
			return getValue();
		}

		public int getValue() {
			return 0;
		}
	}

}
