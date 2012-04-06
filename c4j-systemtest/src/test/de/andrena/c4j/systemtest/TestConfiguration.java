package de.andrena.c4j.systemtest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.andrena.c4j.Configuration;
import de.andrena.c4j.DefaultConfiguration;

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
