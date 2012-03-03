package de.andrena.next.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.next.Configuration;
import de.andrena.next.Configuration.ContractViolationAction;
import de.andrena.next.Configuration.DefaultPreCondition;
import de.andrena.next.Configuration.InvalidPreConditionBehavior;
import de.andrena.next.Configuration.PureBehavior;
import de.andrena.next.internal.util.WhitelistConverter;

public class RuntimeConfiguration {

	private Set<CtMethod> whitelistMethods;
	private Configuration configuration;
	private Set<String> rootPackages = new HashSet<String>();
	private Map<String, String> externalContracts = new HashMap<String, String>();

	public RuntimeConfiguration(Configuration configuration, WhitelistConverter whitelistConverter) throws Exception {
		this.configuration = configuration;
		whitelistMethods = whitelistConverter.convertWhitelist(configuration.getPureWhitelist());
		normalizeRootPackages();
		stringifyExternalContracts();
	}

	private void stringifyExternalContracts() {
		for (Class<?> targetClass : configuration.getExternalContracts().keySet()) {
			externalContracts.put(targetClass.getName(), configuration.getExternalContracts().get(targetClass)
					.getName());
		}
	}

	private void normalizeRootPackages() {
		for (String rootPackage : configuration.getRootPackages()) {
			if (!rootPackage.endsWith(".")) {
				rootPackage += ".";
			}
			rootPackages.add(rootPackage);
		}
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
		return rootPackages;
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

	public Set<ContractViolationAction> getContractViolationActions() {
		return configuration.getContractViolationActions();
	}

	public CtClass getExternalContract(ClassPool pool, CtClass type) throws NotFoundException {
		if (externalContracts.containsKey(type.getName())) {
			return pool.get(externalContracts.get(type.getName()));
		}
		return null;
	}

	public Set<PureBehavior> getPureBehaviors() {
		return configuration.getPureBehaviors();
	}
}
