package de.andrena.next;

import java.lang.reflect.Member;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
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
		pureWhitelist.add(ArrayList.class.getMethod("size"));
		pureWhitelist.add(ArrayList.class.getMethod("get", int.class));
		pureWhitelist.add(ArrayList.class.getMethod("isEmpty"));
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

}
