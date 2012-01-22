package de.andrena.next.systemtest.configuration;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.systemtest.TransformerAwareRule;

public class ConfigurationSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testConfiguration() throws SecurityException, NoSuchMethodException {
	}

}
