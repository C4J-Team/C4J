package de.andrena.c4j.internal.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.andrena.c4j.internal.util.LocalClassLoader;

public class LocalClassLoaderTest {
	@Test
	public void testClassLoading() throws Throwable {
		ClassLoader loader = new LocalClassLoader(getClass().getClassLoader());
		System.out.println("test classloader: " + getClass().getClassLoader());
		System.out.println("context classloader: " + Thread.currentThread().getContextClassLoader());
		Class<?> localClass = Class.forName(getClass().getName() + "$LoadedClass", true, loader);
		Class<?> contextClass = Class.forName(getClass().getName() + "$LoadedClass");
		assertTrue(localClass != contextClass);
	}

	public static class LoadedClass {
	}
}
