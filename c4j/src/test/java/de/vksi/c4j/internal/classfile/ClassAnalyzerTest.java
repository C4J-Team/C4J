package de.vksi.c4j.internal.classfile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import de.vksi.c4j.internal.classfile.ClassAnalyzer;

public class ClassAnalyzerTest {
	private ClassPool pool;

	@Before
	public void before() throws Throwable {
		pool = ClassPool.getDefault();
	}

	@Test
	public void testIsModifiable() throws Throwable {
		CtClass objectClass = pool.get(Object.class.getName());
		assertTrue(ClassAnalyzer.isModifiable(objectClass.getDeclaredMethod("finalize")));
		assertFalse(ClassAnalyzer.isModifiable(objectClass.getDeclaredMethod("hashCode")));
	}

	@Test
	public void testIsDynamic() throws Throwable {
		assertTrue(ClassAnalyzer.isDynamic(pool.get(Object.class.getName()).getDeclaredMethod("finalize")));
		assertFalse(ClassAnalyzer.isDynamic(pool.get(Arrays.class.getName()).getDeclaredMethod("asList")));
	}

}
