package de.andrena.next;

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Set;

public class DefaultConfiguration implements Configuration {
	@Override
	public Set<String> getRootPackages() {
		return Collections.emptySet();
	}

	@Override
	public Set<Member> getPureWhitelist() {
		return Collections.emptySet();
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}
}
