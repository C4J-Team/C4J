package de.vksi.c4j.systemtest.postcondition;

import static de.vksi.c4j.Condition.maxTime;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class MaxTimeSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testMaxTimeSuccess() throws Throwable {
		new TargetClass().sleep(1);
	}

	@Test(expected = AssertionError.class)
	public void testMaxTimeFailure() throws Throwable {
		new TargetClass().sleep(11);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void sleep(long milliSeconds) throws InterruptedException {
			Thread.sleep(milliSeconds);
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void sleep(long milliSeconds) throws InterruptedException {
			if (postCondition()) {
				assert maxTime(0.01);
			}
		}
	}

}
