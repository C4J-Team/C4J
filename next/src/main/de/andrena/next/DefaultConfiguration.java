package de.andrena.next;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public class DefaultConfiguration implements Configuration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.emptySet();
	}

	@Override
	public Set<Method> getPureWhitelist() {
		return Collections.emptySet();
	}
}
