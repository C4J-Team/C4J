package de.vksi.c4j.systemtest.postcondition;

import static de.vksi.c4j.Condition.exceptionThrownOfType;
import static de.vksi.c4j.Condition.postCondition;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PostConditionWithExceptionThrownSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	private SampleClass target;

	@AllowPureAccess
	private static boolean postConditionRan;

	@Before
	public void before() {
		target = new SampleClass();
		postConditionRan = false;
	}

	@Test
	public void testPostConditionWithExceptionThrown() {
		try {
			target.methodThrowingException(true);
			fail("expected IOException");
		} catch (IOException e) {
		}
		assertTrue(postConditionRan);
	}

	@Test
	public void testPostConditionWithExceptionNotThrown() throws Throwable {
		target.methodThrowingException(false);
		assertTrue(postConditionRan);
	}

	@ContractReference(SampleClassContract.class)
	public static class SampleClass {
		public void methodThrowingException(boolean throwException) throws IOException {
			if (throwException) {
				throw new IOException();
			}
		}
	}

	public static class SampleClassContract extends SampleClass {
		@Override
		public void methodThrowingException(boolean throwException) throws IOException {
			if (postCondition()) {
				if (exceptionThrownOfType(IOException.class)) {
					postConditionRan = true;
					assert throwException;
				} else {
					postConditionRan = true;
					assert !throwException;
				}
			}
		}
	}
}
