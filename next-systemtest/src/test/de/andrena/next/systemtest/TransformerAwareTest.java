package de.andrena.next.systemtest;

import org.junit.After;

import de.andrena.next.internal.RootTransformer;

public abstract class TransformerAwareTest {
	@After
	public void after() throws Throwable {
		if (RootTransformer.getLastException() != null) {
			throw RootTransformer.getLastException();
		}
	}
}
