package de.andrena.next.internal;

import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.next.Configuration;
import de.andrena.next.DefaultConfiguration;
import de.andrena.next.internal.util.WhitelistConverter;

public class ConfigurationManager {
	private Set<RuntimeConfiguration> configurations = new HashSet<RuntimeConfiguration>();
	private RuntimeConfiguration defaultConfiguration;
	private Logger logger = Logger.getLogger(getClass());

	public ConfigurationManager(Configuration configuration, ClassPool pool) throws Exception {
		WhitelistConverter whitelistConverter = new WhitelistConverter(pool);
		configurations.add(new RuntimeConfiguration(configuration, whitelistConverter));
		addSubConfigurations(configuration, whitelistConverter);
		defaultConfiguration = new RuntimeConfiguration(new DefaultConfiguration(), whitelistConverter);
	}

	private void addSubConfigurations(Configuration configuration, WhitelistConverter whitelistConverter)
			throws Exception {
		for (Configuration subConfiguration : configuration.getConfigurations()) {
			configurations.add(new RuntimeConfiguration(subConfiguration, whitelistConverter));
			addSubConfigurations(subConfiguration, whitelistConverter);
		}
	}

	public RuntimeConfiguration getConfiguration(CtClass clazz) {
		RuntimeConfiguration responsibleConfiguration = null;
		String longestRootPackage = "";
		for (RuntimeConfiguration configuration : configurations) {
			for (String rootPackage : configuration.getRootPackages()) {
				if (clazz.getName().startsWith(rootPackage) && rootPackage.length() > longestRootPackage.length()) {
					responsibleConfiguration = configuration;
					longestRootPackage = rootPackage;
				}
			}
		}
		if (responsibleConfiguration == null) {
			return defaultConfiguration;
		}
		return responsibleConfiguration;
	}

	public boolean isWithinRootPackages(String className) {
		for (RuntimeConfiguration configuration : configurations) {
			for (String rootPackage : configuration.getRootPackages()) {
				if (className.startsWith(rootPackage)) {
					return true;
				}
			}
		}
		return false;
	}

	public Set<String> getInvolvedClassNames(ClassPool pool) throws NotFoundException {
		Set<String> involvedClassNames = new HashSet<String>();
		for (RuntimeConfiguration configuration : configurations) {
			involvedClassNames.addAll(configuration.getInvolvedClassNames(pool));
		}
		for (RuntimeConfiguration configuration : configurations) {
			involvedClassNames.remove(configuration.getConfigurationClass().getName());
		}
		return involvedClassNames;
	}

}
