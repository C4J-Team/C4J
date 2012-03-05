package de.andrena.c4j;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PureRegistryType {
	private Class<?> type;
	private Set<Method> pureMethods = new HashSet<Method>();
	private Set<Method> unpureMethods = new HashSet<Method>();

	PureRegistryType(Class<?> type) {
		this.type = type;
	}

	Set<Method> getPureMethods() {
		return pureMethods;
	}

	Set<Method> getUnpureMethods() {
		return unpureMethods;
	}

	/**
	 * Adds the declared method with the specified name and parameters as being pure to this type.
	 * 
	 * @param methodName
	 *            The name of the pure method.
	 * @param parameterTypes
	 *            The parameter types of the pure method.
	 * @return The type itself to allow chaining calls.
	 * @throws PureRegistryException
	 *             If reflection can't find or access the given method. Also, if the method was already declared
	 *             pure/unpure.
	 */
	public PureRegistryType pureMethod(String methodName, Class<?>... parameterTypes) throws PureRegistryException {
		addMethod(pureMethods, methodName, parameterTypes);
		return this;
	}

	private void addMethod(Set<Method> set, String methodName, Class<?>... parameterTypes) throws PureRegistryException {
		Method method;
		try {
			method = type.getDeclaredMethod(methodName, parameterTypes);
		} catch (Exception e) {
			throw new PureRegistryException(e);
		}
		handleAlreadyDeclared(method);
		set.add(method);
	}

	private void handleAlreadyDeclared(Method method) throws PureRegistryException {
		if (pureMethods.contains(method) || unpureMethods.contains(method)) {
			throw new PureRegistryException("Method " + method + " was already declared.");
		}
	}

	/**
	 * Adds the declared method(s) with the specified name as being pure to this type, regardless of their parameter
	 * types.
	 * 
	 * @param methodName
	 *            The name of the pure method(s).
	 * @return The type itself to allow chaining calls.
	 * @throws PureRegistryException
	 *             If reflection can't find any method with this name or access any of those methods. Also, if any
	 *             method was already declared pure/unpure.
	 */
	public PureRegistryType pureMethods(String methodName) throws PureRegistryException {
		addMethods(pureMethods, methodName);
		return this;
	}

	private void addMethods(Set<Method> set, String methodName) throws PureRegistryException {
		boolean found = false;
		try {
			for (Method method : type.getDeclaredMethods()) {
				if (method.getName().equals(methodName)) {
					handleAlreadyDeclared(method);
					set.add(method);
					found = true;
				}
			}
		} catch (PureRegistryException e) {
			throw e;
		} catch (Exception e) {
			throw new PureRegistryException(e);
		}
		if (!found) {
			throw new PureRegistryException("No method found with name " + methodName + ".");
		}
	}

	/**
	 * Adds the declared method with the specified name and parameters as being unpure to this type.
	 * 
	 * @param methodName
	 *            The name of the unpure method.
	 * @param parameterTypes
	 *            The parameter types of the unpure method.
	 * @return The type itself to allow chaining calls.
	 * @throws PureRegistryException
	 *             If reflection can't find or access the given method. Also, if the method was already declared
	 *             pure/unpure.
	 */
	public PureRegistryType unpureMethod(String methodName, Class<?>... parameterTypes) throws PureRegistryException {
		addMethod(unpureMethods, methodName, parameterTypes);
		return this;
	}

	/**
	 * Adds the declared method(s) with the specified name as being unpure to this type, regardless of their parameter
	 * types.
	 * 
	 * @param methodName
	 *            The name of the unpure method(s).
	 * @return The type itself to allow chaining calls.
	 * @throws PureRegistryException
	 *             If reflection can't find any method with this name or access any of those methods. Also, if any
	 *             method was already declared pure/unpure.
	 */
	public PureRegistryType unpureMethods(String methodName) throws PureRegistryException {
		addMethods(unpureMethods, methodName);
		return this;
	}

	/**
	 * Adds all declared methods as being pure to this type.
	 * 
	 * @return The type itself to allow chaining calls.
	 * @throws PureRegistryException
	 *             If reflection can't access any of those methods. Also, if any method was already declared
	 *             pure/unpure.
	 */
	public PureRegistryType onlyPureMethods() throws PureRegistryException {
		addAllMethods(pureMethods);
		return this;
	}

	private void addAllMethods(Set<Method> set) throws PureRegistryException {
		try {
			for (Method method : type.getDeclaredMethods()) {
				handleAlreadyDeclared(method);
				set.add(method);
			}
		} catch (PureRegistryException e) {
			throw e;
		} catch (Exception e) {
			throw new PureRegistryException(e);
		}
	}

	/**
	 * Adds all declared methods as being unpure to this type.
	 * 
	 * @return The type itself to allow chaining calls.
	 * @throws PureRegistryException
	 *             If reflection can't access any of those methods. Also, if any method was already declared
	 *             pure/unpure.
	 */
	public PureRegistryType onlyUnpureMethods() throws PureRegistryException {
		addAllMethods(unpureMethods);
		return this;
	}
}