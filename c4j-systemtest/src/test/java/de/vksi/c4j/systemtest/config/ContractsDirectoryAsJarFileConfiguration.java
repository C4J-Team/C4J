package de.vksi.c4j.systemtest.config;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;

public class ContractsDirectoryAsJarFileConfiguration extends AbstractConfiguration {

	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j.systemtest.config.contractsdirectoryasjarfile");
	}

	@Override
	public File getContractsDirectory() {
		return new File("lib/contractsdirectoryasjarfile.jar");
	}

}
