package de.andrena.next.systemtest;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.external.ExternalClass;

import de.andrena.next.Configuration;
import de.andrena.next.DefaultConfiguration;
import de.andrena.next.systemtest.config.AssertionErrorOnlyConfiguration;
import de.andrena.next.systemtest.config.DefaultPreConditionTrueConfiguration;
import de.andrena.next.systemtest.config.ExternalContractConfiguration;
import de.andrena.next.systemtest.config.InvalidPreConditionBehaviorErrorConfiguration;
import de.andrena.next.systemtest.config.LogOnlyConfiguration;

public class SystemTestConfiguration extends DefaultConfiguration {
	@Override
	public Set<Configuration> getConfigurations() {
		Set<Configuration> configurations = new HashSet<Configuration>();
		configurations.add(new DefaultPreConditionTrueConfiguration());
		configurations.add(new InvalidPreConditionBehaviorErrorConfiguration());
		configurations.add(new LogOnlyConfiguration());
		configurations.add(new AssertionErrorOnlyConfiguration());
		configurations.add(new ExternalContractConfiguration());
		return configurations;
	}

	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.andrena.next.systemtest");
	}

	@Override
	public boolean writeTransformedClasses() {
		return false;
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
		pureWhitelist.add(ExternalClass.class.getMethod("unpureMethodWhitelistedInConfig"));
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

	public void definePureWhitelist() throws Throwable {
		pureWhitelist(ArrayList.class).size();
		pureWhitelist(ArrayList.class).get(0);
		pureWhitelist(ArrayList.class).isEmpty();
		// aber:
		pureWhitelist(String.class).regionMatches(false, 0, null, 0, 0);
		// konstruktoren?
		//		pureWhitelistConstructor(String.class).withArguments(byte[].class, Charset.class);
		// oder:
		pureWhitelistConstructor(new String(null, (Charset) null));
		// statische methoden = warning
		pureWhitelist(String.class).format(null);
	}

	protected <T> T pureWhitelist(Class<T> clazz) {
		return null;
	}

	protected <T> void pureWhitelistConstructor(T constructorCall) {
	}

	/*	protected WhitelistConstructor pureWhitelistConstructor(Class<?> clazz) {
			return new WhitelistConstructor();
		}
		
		public static class WhitelistConstructor {
			public void withNoArguments() {
			}

			public void withArguments(Class<?> firstArgument, Class<?>... additionalArguments) {
			}
		}*/
}
