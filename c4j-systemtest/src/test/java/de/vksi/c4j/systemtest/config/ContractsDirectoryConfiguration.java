package de.vksi.c4j.systemtest.config;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;

public class ContractsDirectoryConfiguration extends AbstractConfiguration {

	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j.systemtest.config.contractsdirectory");
	}

	@Override
	public File getContractsDirectory() {
		return new File("target/test-classes/de/vksi/c4j/systemtest/config/contractsdirectory");
	}

}
