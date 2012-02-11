package de.andrena.next.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.next.Configuration;
import de.andrena.next.internal.util.WhitelistConverter;

public class ConfigurationManager {
	private Set<RuntimeConfiguration> configurations = new HashSet<RuntimeConfiguration>();
	private Logger logger = Logger.getLogger(getClass());

	public ConfigurationManager(Configuration configuration, ClassPool pool) throws Exception {
		WhitelistConverter whitelistConverter = new WhitelistConverter(pool);
		configurations.add(new RuntimeConfiguration(configuration, whitelistConverter));
		addSubConfigurations(configuration, whitelistConverter);
	}

	private void addSubConfigurations(Configuration configuration, WhitelistConverter whitelistConverter)
			throws Exception {
		for (Configuration subConfiguration : configuration.getConfigurations()) {
			configurations.add(new RuntimeConfiguration(subConfiguration, whitelistConverter));
			addSubConfigurations(subConfiguration, whitelistConverter);
		}
	}

	private RuntimeConfiguration getResponsibleConfiguration(CtClass clazz) {
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
		return responsibleConfiguration;
	}

	public boolean writeTransformedClass(CtClass clazz) {
		RuntimeConfiguration responsibleConfiguration = getResponsibleConfiguration(clazz);
		return responsibleConfiguration != null && responsibleConfiguration.writeTransformedClasses();
	}

	public Set<CtMethod> getWhitelistMethods(CtClass clazz) {
		RuntimeConfiguration responsibleConfiguration = getResponsibleConfiguration(clazz);
		if (responsibleConfiguration == null) {
			return Collections.emptySet();
		}
		return responsibleConfiguration.getWhitelistMethods();
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
