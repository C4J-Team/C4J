package de.andrena.c4j;

import java.util.HashSet;
import java.util.Set;

import de.andrena.c4j.systemtest.SystemTestConfiguration;
import de.andrena.c4j.Configuration;
import de.andrena.c4j.DefaultConfiguration;

public class TestConfiguration extends DefaultConfiguration {

	@Override
	public Set<Configuration> getConfigurations() {
		Set<Configuration> configurations = new HashSet<Configuration>();
		configurations.add(new AcceptanceTestConfiguration());
		configurations.add(new SystemTestConfiguration());
		return configurations;
	}

}
