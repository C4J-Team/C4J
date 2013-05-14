package de.vksi.c4j.systemtest.postcompiler;

import java.io.File;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.vksi.c4j.main.PreTransformer;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PreTransformerITCase {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Rule
	public TemporaryFolder destinationDir = new TemporaryFolder();

	@Test
	public void testPreTransformer() throws Throwable {
		// cannot run together with all the other tests
		Assume.assumeTrue(!TransformerAwareRule.haveTestsRun());
		PreTransformer preTransformer = new PreTransformer(new File("."), destinationDir.getRoot());
		preTransformer.transformAllClassFiles();
	}
}
