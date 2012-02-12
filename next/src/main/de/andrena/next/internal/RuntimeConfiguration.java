package de.andrena.next.internal;

import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.Configuration;
import de.andrena.next.Configuration.DefaultPreCondition;
import de.andrena.next.Configuration.InvalidPreConditionBehavior;
import de.andrena.next.internal.util.WhitelistConverter;

public class RuntimeConfiguration {

	private Set<CtMethod> whitelistMethods;
	private Configuration configuration;

	public RuntimeConfiguration(Configuration configuration, WhitelistConverter whitelistConverter) throws Exception {
		this.configuration = configuration;
		whitelistMethods = whitelistConverter.convertWhitelist(configuration.getPureWhitelist());
	}

	public Class<?> getConfigurationClass() {
		return configuration.getClass();
	}

	public Set<String> getInvolvedClassNames(ClassPool pool) throws NotFoundException {
		@SuppressWarnings("unchecked")
		Set<String> classNamesWithSlashes = pool.get(getConfigurationClass().getName()).getClassFile().getConstPool()
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
		return configuration.getRootPackages();
	}

	public boolean writeTransformedClasses() {
		return configuration.writeTransformedClasses();
	}

	public DefaultPreCondition getDefaultPreCondition() {
		return configuration.getDefaultPreCondition();
	}

	public InvalidPreConditionBehavior getInvalidPreConditionBehavior() {
		return configuration.getInvalidPreConditionBehavior();
	}
}
