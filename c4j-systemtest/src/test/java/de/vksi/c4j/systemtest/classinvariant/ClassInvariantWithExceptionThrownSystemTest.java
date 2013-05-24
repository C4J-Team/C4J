package de.vksi.c4j.systemtest.classinvariant;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ClassInvariantWithExceptionThrownSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	private SampleClass target;

	@AllowPureAccess
	private static boolean invariantRan;

	@Before
	public void before() {
		target = new SampleClass();
		invariantRan = false;
	}

	@Test
	public void testClassInvariantWithExceptionThrown() {
		try {
			target.methodThrowingException(true);
			fail("expected IOException");
		} catch (IOException e) {
		}
		assertTrue(invariantRan);
	}

	@Test
	public void testClassInvariantWithExceptionNotThrown() throws Throwable {
		target.methodThrowingException(false);
		assertTrue(invariantRan);
	}

	@ContractReference(SampleClassContract.class)
	private static class SampleClass {
		public void methodThrowingException(boolean throwException) throws IOException {
			if (throwException) {
				throw new IOException();
			}
		}
	}

	@SuppressWarnings("unused")
	private static class SampleClassContract extends SampleClass {
		@ClassInvariant
		public void invariant() {
			invariantRan = true;
		}
	}
}
