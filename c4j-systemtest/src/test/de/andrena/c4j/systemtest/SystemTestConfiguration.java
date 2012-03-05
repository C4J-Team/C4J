package de.andrena.c4j.systemtest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.external.ExternalClass;

import de.andrena.c4j.Configuration;
import de.andrena.c4j.DefaultConfiguration;
import de.andrena.c4j.PureRegistry;
import de.andrena.c4j.PureRegistryException;
import de.andrena.c4j.systemtest.config.AssertionErrorOnlyConfiguration;
import de.andrena.c4j.systemtest.config.DefaultPreConditionTrueConfiguration;
import de.andrena.c4j.systemtest.config.ExternalContractConfiguration;
import de.andrena.c4j.systemtest.config.LogOnlyConfiguration;
import de.andrena.c4j.systemtest.config.PureBehaviorEmptyConfiguration;
import de.andrena.c4j.systemtest.config.PureBehaviorSkipOnlyConfiguration;
import de.andrena.c4j.systemtest.config.StrengtheningPreConditionAllowedConfiguration;

public class SystemTestConfiguration extends DefaultConfiguration {
	@Override
	public Set<Configuration> getConfigurations() {
		Set<Configuration> configurations = new HashSet<Configuration>();
		configurations.add(new DefaultPreConditionTrueConfiguration());
		configurations.add(new StrengtheningPreConditionAllowedConfiguration());
		configurations.add(new LogOnlyConfiguration());
		configurations.add(new AssertionErrorOnlyConfiguration());
		configurations.add(new ExternalContractConfiguration());
		configurations.add(new PureBehaviorSkipOnlyConfiguration());
		configurations.add(new PureBehaviorEmptyConfiguration());
		return configurations;
	}

	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.systemtest");
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

	@Override
	public PureRegistry getPureRegistry() throws PureRegistryException {
		return PureRegistry.union(
				PureRegistry.register(Boolean.class)
						.pureMethod("booleanValue"),
				PureRegistry.register(Class.class)
						.pureMethod("desiredAssertionStatus")
						.pureMethod("getName"),
				PureRegistry.register(Collection.class)
						.pureMethod("size")
						.pureMethod("isEmpty"),
				PureRegistry.register(ExternalClass.class)
						.pureMethod("pureMethodWhitelistedInConfig")
						.unpureMethod("unpureMethodBlacklistedInConfig"),
				PureRegistry.register(Integer.class)
						.pureMethod("intValue")
						.pureMethod("valueOf", int.class),
				PureRegistry.register(List.class)
						.pureMethod("get", int.class),
				PureRegistry.register(Object.class)
						.pureMethod("getClass")
						.pureMethod("equals", Object.class)
						.pureMethod("hashCode")
						.pureMethod("toString"),
				PureRegistry.register(StackTraceElement.class)
						.pureMethod("getClassName"),
				PureRegistry.register(String.class)
						.pureMethod("valueOf", Object.class),
				PureRegistry.register(Throwable.class)
						.pureMethod("getStackTrace")
				);
	}
}
