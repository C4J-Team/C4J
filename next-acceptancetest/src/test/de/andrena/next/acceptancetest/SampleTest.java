package de.andrena.next.acceptancetest;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.systemtest.TransformerAwareRule;

public class SampleTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testSample() {
		assertTrue(true);
	}
}
