package de.andrena.next.internal;

import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.Configuration;
import de.andrena.next.internal.util.WhitelistConverter;

public class RuntimeConfiguration {

	private Class<?> configurationClass;
	private Set<CtMethod> whitelistMethods;
	private Set<String> rootPackages;
	private boolean writeTransformedClasses;

	public RuntimeConfiguration(Configuration configuration, WhitelistConverter whitelistConverter) throws Exception {
		configurationClass = configuration.getClass();
		rootPackages = configuration.getRootPackages();
		writeTransformedClasses = configuration.writeTransformedClasses();
		whitelistMethods = whitelistConverter.convertWhitelist(configuration.getPureWhitelist());
	}

	public Class<?> getConfigurationClass() {
		return configurationClass;
	}

	public Set<String> getInvolvedClassNames(ClassPool pool) throws NotFoundException {
		@SuppressWarnings("unchecked")
		Set<String> classNamesWithSlashes = pool.get(configurationClass.getName()).getClassFile().getConstPool()
				.getClassNames();
		Set<String> involvedClassNames = new HashSet<String>();
		for (String classNameWithSlashes : classNamesWithSlashes) {
			involvedClassNames.add(classNameWithSlashes.replace('/', '.'));
		}
		return involvedClassNames;
	}

	public Set<CtMethod> getWhitelistMethods() {
		return whitelistMethods;
	}

	public Set<String> getRootPackages() {
		return rootPackages;
	}

	public boolean writeTransformedClasses() {
		return writeTransformedClasses;
	}
}
