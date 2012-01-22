package de.andrena.next;

import java.lang.reflect.Method;
import java.util.Set;

public interface Configuration {
	Set<String> getRootPackages();

	Set<Method> getPureWhitelist() throws NoSuchMethodException, SecurityException;
}
