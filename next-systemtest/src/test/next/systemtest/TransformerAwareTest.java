package next.systemtest;

import next.internal.RootTransformer;

import org.junit.After;

public abstract class TransformerAwareTest {
	@After
	public void after() throws Throwable {
		if (RootTransformer.getLastException() != null) {
			throw RootTransformer.getLastException();
		}
	}
}
