package de.vksi.c4j.systemtest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;
import de.vksi.c4j.PureRegistry;
import de.vksi.c4j.PureRegistryException;

public class AcceptanceTestConfiguration extends AbstractConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j.acceptancetest");
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
	public Set<PureBehavior> getPureBehaviors() {
		Set<PureBehavior> pureBehaviors = new HashSet<PureBehavior>();
		pureBehaviors.add(PureBehavior.SKIP_INVARIANTS);
		pureBehaviors.add(PureBehavior.VALIDATE_PURE);
		return pureBehaviors;
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

}
