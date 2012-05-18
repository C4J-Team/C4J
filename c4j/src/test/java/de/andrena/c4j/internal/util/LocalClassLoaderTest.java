package de.andrena.c4j.internal.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocalClassLoaderTest {
	@Test
	public void testClassLoading() throws Throwable {
		ClassLoader loader = new LocalClassLoader(getClass().getClassLoader());
		Class<?> localClass = Class.forName("de.andrena.localclassloader.LoadedClass", true, loader);
		Class<?> contextClass = Class.forName("de.andrena.localclassloader.LoadedClass");
		assertTrue(localClass != contextClass);
	}
}
