package de.vksi.c4j.systemtest.config.contractsdirectoryasjarfile;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractsDirectoryAsJarFileSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testContractsDirectory() {
		new TargetClassUsingContractInJarFile().method(0);
	}

	private static class TargetClassUsingContractInJarFile {
		public void method(int arg) {
		}
	}
}
