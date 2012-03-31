package de.andrena.c4j.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import de.andrena.c4j.Configuration;
import de.andrena.c4j.DefaultConfiguration;
import de.andrena.c4j.internal.util.WhitelistConverter;

public class ConfigurationManager {
	private Set<RuntimeConfiguration> configurations = new HashSet<RuntimeConfiguration>();
	private RuntimeConfiguration defaultConfiguration;
	private Map<String, RuntimeConfiguration> rootPackageToConfigurationMap = new HashMap<String, RuntimeConfiguration>();

	public ConfigurationManager(Configuration configuration, ClassPool pool) throws Exception {
		WhitelistConverter whitelistConverter = new WhitelistConverter(pool);
		configurations.add(new RuntimeConfiguration(configuration, whitelistConverter));
		addSubConfigurations(configuration, whitelistConverter);
		defaultConfiguration = new RuntimeConfiguration(new DefaultConfiguration(), whitelistConverter);
		initRootPackages();
	}

	private void initRootPackages() {
		for (RuntimeConfiguration configuration : configurations) {
			for (String rootPackage : configuration.getRootPackages()) {
				if (rootPackageToConfigurationMap.containsKey(rootPackage)) {
					throw new IllegalArgumentException("Two Configurations '" + configuration.getClass().getName()
							+ "' and '"
							+ rootPackageToConfigurationMap.get(rootPackage).getConfigurationClass().getName()
							+ "' contain the same RootPackage '" + rootPackage + "'.");
				}
				rootPackageToConfigurationMap.put(rootPackage, configuration);
			}
		}
	}

	private void addSubConfigurations(Configuration configuration, WhitelistConverter whitelistConverter)
			throws Exception {
		for (Configuration subConfiguration : configuration.getConfigurations()) {
			configurations.add(new RuntimeConfiguration(subConfiguration, whitelistConverter));
			addSubConfigurations(subConfiguration, whitelistConverter);
		}
	}

	public RuntimeConfiguration getConfiguration(CtClass clazz) {
		return getConfiguration(clazz.getName());
	}

	public RuntimeConfiguration getConfiguration(Class<?> clazz) {
		return getConfiguration(clazz.getName());
	}

	public RuntimeConfiguration getConfiguration(String className) {
		String currentPackage = className;
		while (currentPackage.lastIndexOf('.') > -1) {
			currentPackage = className.substring(0, currentPackage.lastIndexOf('.'));
			if (rootPackageToConfigurationMap.containsKey(currentPackage)) {
				return rootPackageToConfigurationMap.get(currentPackage);
			}
		}
		return defaultConfiguration;
	}

	public boolean isWithinRootPackages(String className) {
		return getConfiguration(className) != defaultConfiguration;
	}

	public boolean isWithinRootPackages(CtClass clazz) {
		return isWithinRootPackages(clazz.getName());
	}
}
