package de.andrena.c4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.andrena.c4j.systemtest.SystemTestConfiguration;

public class TestConfiguration extends DefaultConfiguration {

	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j");
	}

	@Override
	public Set<Configuration> getConfigurations() {
		Set<Configuration> configurations = new HashSet<Configuration>();
		configurations.add(new AcceptanceTestConfiguration());
		configurations.add(new SystemTestConfiguration());
		return configurations;
	}

}
