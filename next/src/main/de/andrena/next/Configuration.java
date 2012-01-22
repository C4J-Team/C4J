package de.andrena.next;

import java.lang.reflect.Member;
import java.util.Set;

public interface Configuration {
	Set<String> getRootPackages();

	Set<Member> getPureWhitelist() throws NoSuchMethodException, SecurityException;
}
