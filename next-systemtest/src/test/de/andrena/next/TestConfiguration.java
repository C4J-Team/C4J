package de.andrena.next;

import java.util.HashSet;
import java.util.Set;

import de.andrena.next.systemtest.SystemTestConfiguration;

public class TestConfiguration extends DefaultConfiguration {

	@Override
	public Set<Configuration> getConfigurations() {
		Set<Configuration> configurations = new HashSet<Configuration>();
		configurations.add(new AcceptanceTestConfiguration());
		configurations.add(new SystemTestConfiguration());
		return configurations;
	}

}
