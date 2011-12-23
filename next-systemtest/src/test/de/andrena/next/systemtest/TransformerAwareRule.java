package de.andrena.next.systemtest;

import org.junit.rules.Verifier;

import de.andrena.next.internal.RootTransformer;

public class TransformerAwareRule extends Verifier {

	@Override
	protected void verify() throws Throwable {
		if (RootTransformer.getLastException() != null) {
			throw RootTransformer.getLastException();
		}	
	}
	
}
