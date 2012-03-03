package de.andrena.c4j.systemtest.postcondition;

import static de.andrena.next.Condition.exceptionThrownOfType;
import static de.andrena.next.Condition.post;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.AllowPureAccess;
import de.andrena.next.Contract;

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

	@Contract(SampleClassContract.class)
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
			if (post()) {
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
