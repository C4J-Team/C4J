package de.andrena.next;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AcceptanceTestConfiguration extends DefaultConfiguration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.acceptancetest");
	}

	@Override
	public Set<Method> getPureWhitelist() throws NoSuchMethodException, SecurityException {
		Set<Method> pureWhitelist = new HashSet<Method>();
		pureWhitelist.add(Boolean.class.getMethod("booleanValue"));
		pureWhitelist.add(Class.class.getMethod("desiredAssertionStatus"));
		pureWhitelist.add(Class.class.getMethod("getClass"));
		pureWhitelist.add(Class.class.getMethod("getName"));
		pureWhitelist.add(Collection.class.getMethod("size"));
		pureWhitelist.add(Collection.class.getMethod("isEmpty"));
		pureWhitelist.add(Integer.class.getMethod("intValue"));
		pureWhitelist.add(Integer.class.getMethod("valueOf", int.class));
		pureWhitelist.add(List.class.getMethod("get", int.class));
		pureWhitelist.add(Object.class.getMethod("equals", Object.class));
		pureWhitelist.add(Object.class.getMethod("hashCode"));
		pureWhitelist.add(Object.class.getMethod("toString"));
		pureWhitelist.add(StackTraceElement.class.getMethod("getClassName"));
		pureWhitelist.add(String.class.getMethod("valueOf", Object.class));
		pureWhitelist.add(Throwable.class.getMethod("getStackTrace"));
		return pureWhitelist;
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

}
