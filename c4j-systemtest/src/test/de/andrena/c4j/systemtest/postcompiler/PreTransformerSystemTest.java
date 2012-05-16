package de.andrena.c4j.systemtest.postcompiler;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.andrena.c4j.PreTransformer;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class PreTransformerSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Rule
	public TemporaryFolder destinationDir = new TemporaryFolder();

	@Test
	public void testPreTransformer() throws Throwable {
		PreTransformer preTransformer = new PreTransformer(new File("."), destinationDir.getRoot());
		preTransformer.transformAllClassFiles();
	}
}
