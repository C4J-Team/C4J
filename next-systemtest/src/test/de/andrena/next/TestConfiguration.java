package de.andrena.next;

import java.lang.reflect.Member;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestConfiguration implements Configuration {

	@Override
	public Set<String> getRootPackages() {
		Set<String> rootPackages = new HashSet<String>();
		rootPackages.add("de.andrena.next.acceptancetest");
		rootPackages.add("de.andrena.next.systemtest");
		return rootPackages;
	}

	@Override
	public Set<Member> getPureWhitelist() throws NoSuchMethodException, SecurityException {
		Set<Member> pureWhitelist = new HashSet<Member>();
		pureWhitelist.add(AssertionError.class.getConstructor(Object.class));
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
		pureWhitelist.add(StackTraceElement.class.getMethod("getClassName"));
		pureWhitelist.add(Throwable.class.getMethod("getStackTrace"));
		return pureWhitelist;
	}

	public void definePureWhitelist() throws Throwable {
		pureWhitelist(ArrayList.class).size();
		pureWhitelist(ArrayList.class).get(0);
		pureWhitelist(ArrayList.class).isEmpty();
		// aber:
		pureWhitelist(String.class).regionMatches(false, 0, null, 0, 0);
		// konstruktoren?
		pureWhitelistConstructor(String.class).withArguments(byte[].class, Charset.class);
		// oder:
		pureWhitelistConstructor(new String(null, (Charset) null));
		// statische methoden = warning
		pureWhitelist(String.class).format(null);
	}

	protected <T> T pureWhitelist(Class<T> clazz) {
		return null;
	}

	protected WhitelistConstructor pureWhitelistConstructor(Class<?> clazz) {
		return new WhitelistConstructor();
	}

	protected <T> void pureWhitelistConstructor(T constructorCall) {
	}

	public static class WhitelistConstructor {
		public void withNoArguments() {
		}

		public void withArguments(Class<?> firstArgument, Class<?>... additionalArguments) {
		}
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
	}

}
