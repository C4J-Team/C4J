package de.vksi.c4j.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.configuration.C4JPureRegistry;
import de.vksi.c4j.internal.configuration.C4JPureRegistry.Type;
import de.vksi.c4j.internal.configuration.Empty;
import de.vksi.c4j.internal.configuration.MethodByName;
import de.vksi.c4j.internal.configuration.MethodBySignature;

public class PureRegistryImporter {
	private ClassPool pool = RootTransformer.INSTANCE.getPool();
	private final URL pureRegistryUrl;
	private Logger logger = Logger.getLogger(PureRegistryImporter.class);
	private Set<CtMethod> whitelistMethods = new HashSet<CtMethod>();
	private Set<CtMethod> blacklistMethods = new HashSet<CtMethod>();

	public Set<CtMethod> getWhitelistMethods() {
		return whitelistMethods;
	}

	public Set<CtMethod> getBlacklistMethods() {
		return blacklistMethods;
	}

	public PureRegistryImporter(C4JPureRegistry pureRegistry, URL pureRegistryUrl) {
		this.pureRegistryUrl = pureRegistryUrl;
		for (Type type : pureRegistry.getType()) {
			importType(type);
		}
	}

	private void importType(Type type) {
		try {
			CtClass typeClass = pool.get(type.getName());
			importExistingType(type, typeClass);
		} catch (NotFoundException e) {
			logger.error("Could not find type " + type.getName() + " for pure-registry " + pureRegistryUrl);
			return;
		}
	}

	private void importExistingType(Type type, CtClass typeClass) {
		checkAndAddAllMethods(type.getOnlyPureMethods(), typeClass, whitelistMethods);
		checkAndAddAllMethods(type.getOnlyUnpureMethods(), typeClass, blacklistMethods);
		addMethodsBySignature(type.getPureMethod(), typeClass, whitelistMethods);
		addMethodsBySignature(type.getUnpureMethod(), typeClass, blacklistMethods);
		addMethodsByName(type.getPureMethodByName(), typeClass, whitelistMethods);
		addMethodsByName(type.getUnpureMethodByName(), typeClass, blacklistMethods);
	}

	private void addMethodsByName(List<MethodByName> methods, CtClass typeClass, Set<CtMethod> list) {
		for (MethodByName method : methods) {
			addMethodByName(typeClass, list, method.getName());
		}
	}

	private void addMethodByName(CtClass typeClass, Set<CtMethod> list, String methodName) {
		Set<CtMethod> matchingMethods = findMethodsWithName(typeClass, methodName);
		if (matchingMethods.isEmpty()) {
			logger.error("Could not find method " + methodName + " in type " + typeClass.getName()
					+ " for pure-registry " + pureRegistryUrl);
			return;
		}
		list.addAll(matchingMethods);
	}

	private Set<CtMethod> findMethodsWithName(CtClass typeClass, String methodName) {
		Set<CtMethod> matchingMethods = new HashSet<CtMethod>();
		for (CtMethod typeMethod : typeClass.getDeclaredMethods()) {
			if (typeMethod.getName().equals(methodName)) {
				matchingMethods.add(typeMethod);
			}
		}
		return matchingMethods;
	}

	private void addMethodsBySignature(List<MethodBySignature> methods, CtClass typeClass, Set<CtMethod> list) {
		for (MethodBySignature method : methods) {
			addMethodBySignature(typeClass, list, method.getSignature());
		}
	}

	private void addMethodBySignature(CtClass typeClass, Set<CtMethod> list, String signature) {
		try {
			list.add(getMethodBySignature(signature, typeClass));
		} catch (NotFoundException e) {
			logger.error("Could not find method " + signature + " in type " + typeClass.getName()
					+ " for pure-registry " + pureRegistryUrl);
		}
	}

	private CtMethod getMethodBySignature(String signature, CtClass typeClass) throws NotFoundException {
		return typeClass.getDeclaredMethod(getMethodName(signature), getMethodParameters(signature).toArray(
					new CtClass[0]));
	}

	private String getMethodName(String signature) {
		return signature.substring(0, signature.indexOf('('));
	}

	private List<CtClass> getMethodParameters(String signature) throws NotFoundException {
		String parametersAsString = signature.substring(signature.indexOf('(') + 1, signature.indexOf(')'));
		String[] parameters = parametersAsString.split(Pattern.quote(","));
		List<CtClass> methodParameters = new ArrayList<CtClass>();
		for (String parameter : parameters) {
			checkAndAddMethodParameter(methodParameters, parameter);
		}
		return methodParameters;
	}

	private void checkAndAddMethodParameter(List<CtClass> methodParameters, String parameter) throws NotFoundException {
		parameter = parameter.trim();
		if (!parameter.isEmpty()) {
			methodParameters.add(pool.get(parameter));
		}
	}

	private void checkAndAddAllMethods(Empty pureMethods, CtClass typeClass, Set<CtMethod> list) {
		if (pureMethods != null) {
			list.addAll(Arrays.asList(typeClass.getDeclaredMethods()));
		}
	}

}
