package de.andrena.next.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

public class ReflectionHelperTest {
	private ReflectionHelper helper;
	private ClassPool pool;

	@Before
	public void before() {
		helper = new ReflectionHelper();
		pool = ClassPool.getDefault();
	}

	@Test
	public void testGetDeclaredModifiableMethods() throws Throwable {
		assertEquals(5, helper.getDeclaredModifiableMethods(pool.get(Object.class.getName())).size());
		assertEquals(0, helper.getDeclaredModifiableMethods(pool.get(List.class.getName())).size());
	}

	@Test
	public void testGetDeclaredModifiableDynamicMethods() throws Throwable {
		assertEquals(5, helper.getDeclaredModifiableDynamicMethods(pool.get(Object.class.getName())).size());
		assertEquals(0, helper.getDeclaredModifiableDynamicMethods(pool.get(List.class.getName())).size());
		assertEquals(0, helper.getDeclaredModifiableDynamicMethods(pool.get(Collections.class.getName())).size());
	}

	@Test
	public void testIsModifiable() throws Throwable {
		CtClass objectClass = pool.get(Object.class.getName());
		assertTrue(helper.isModifiable(objectClass.getDeclaredMethod("finalize")));
		assertFalse(helper.isModifiable(objectClass.getDeclaredMethod("hashCode")));
	}

	@Test
	public void testIsDynamic() throws Throwable {
		assertTrue(helper.isDynamic(pool.get(Object.class.getName()).getDeclaredMethod("finalize")));
		assertFalse(helper.isDynamic(pool.get(Arrays.class.getName()).getDeclaredMethod("asList")));
	}
}
