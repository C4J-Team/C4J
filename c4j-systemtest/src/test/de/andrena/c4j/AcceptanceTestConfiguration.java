package de.andrena.c4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AcceptanceTestConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.c4j.acceptancetest");
	}

	@Override
	public PureRegistry getPureRegistry() throws PureRegistryException {
		return PureRegistry.union(
				PureRegistry.register(Boolean.class)
						.pureMethod("booleanValue"),
				PureRegistry.register(Class.class)
						.pureMethod("desiredAssertionStatus")
						.pureMethod("getName"),
				PureRegistry.register(Collection.class)
						.pureMethod("size")
						.pureMethod("isEmpty"),
				PureRegistry.register(Integer.class)
						.pureMethod("intValue")
						.pureMethod("valueOf", int.class),
				PureRegistry.register(List.class)
						.pureMethod("get", int.class),
				PureRegistry.register(Object.class)
						.pureMethod("getClass")
						.pureMethod("equals", Object.class)
						.pureMethod("hashCode")
						.pureMethod("toString"),
				PureRegistry.register(StackTraceElement.class)
						.pureMethod("getClassName"),
				PureRegistry.register(String.class)
						.pureMethod("valueOf", Object.class),
				PureRegistry.register(Throwable.class)
						.pureMethod("getStackTrace")
				);
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

}
