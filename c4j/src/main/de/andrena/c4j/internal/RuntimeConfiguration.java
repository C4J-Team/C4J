package de.andrena.c4j.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import de.andrena.c4j.Configuration;
import de.andrena.c4j.Configuration.ContractViolationAction;
import de.andrena.c4j.Configuration.DefaultPreCondition;
import de.andrena.c4j.Configuration.PureBehavior;
import de.andrena.c4j.internal.util.WhitelistConverter;

public class RuntimeConfiguration {

	private Set<CtMethod> whitelistMethods;
	private Configuration configuration;
	private Map<String, String> externalContracts = new HashMap<String, String>();
	private Set<CtMethod> blacklistMethods;

	public RuntimeConfiguration(Configuration configuration, WhitelistConverter whitelistConverter) throws Exception {
		this.configuration = configuration;
		whitelistMethods = whitelistConverter.convertWhitelist(configuration.getPureRegistry().getPureMethods());
		blacklistMethods = whitelistConverter.convertWhitelist(configuration.getPureRegistry().getUnpureMethods());
		stringifyExternalContracts();
	}

	private void stringifyExternalContracts() {
		for (Class<?> targetClass : configuration.getExternalContracts().keySet()) {
			externalContracts.put(targetClass.getName(), configuration.getExternalContracts().get(targetClass)
					.getName());
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

	public Set<CtMethod> getBlacklistMethods() {
		return blacklistMethods;
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

	public boolean isStrengtheningPreConditionAllowed() {
		return configuration.isStrengtheningPreConditionAllowed();
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
