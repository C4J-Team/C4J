package de.andrena.c4j;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PureRegistry {
	private Set<Method> pureMethods = new HashSet<Method>();
	private Set<Method> unpureMethods = new HashSet<Method>();

	private PureRegistry(PureRegistryType... types) {
		for (PureRegistryType type : types) {
			pureMethods.addAll(type.getPureMethods());
			unpureMethods.addAll(type.getUnpureMethods());
		}
	}

	/**
	 * Creates a {@link PureRegistryType}, allowing the definition of the pure and unpure methods of the specified type.
	 * 
	 * @param type
	 *            The type (class or interface) for which pure and unpure methods are being defined.
	 * @return The corresponding {@link PureRegistryType}.
	 */
	public static PureRegistryType register(Class<?> type) {
		return new PureRegistryType(type);
	}

	/**
	 * Creates a {@link PureRegistry}, allowing the definition of the pure and unpure methods of multiple types.
	 * 
	 * @param types
	 *            The types (classes and interfaces) for which pure and unpure methods are being defined.
	 * @return The corresponding {@link PureRegistry}.
	 */
	public static PureRegistry union(PureRegistryType... types) {
		return new PureRegistry(types);
	}

	public Set<Method> getPureMethods() {
		return pureMethods;
	}

	public Set<Method> getUnpureMethods() {
		return unpureMethods;
	}

}