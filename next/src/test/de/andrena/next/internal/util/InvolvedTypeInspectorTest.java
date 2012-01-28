package de.andrena.next.internal.util;

import static org.junit.Assert.assertEquals;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.Contract;
import de.andrena.next.internal.RootTransformerTest.SuperClass;

public class InvolvedTypeInspectorTest {

	private ClassPool pool;
	private InvolvedTypeInspector inspector;

	@Before
	public void before() {
		pool = ClassPool.getDefault();
		inspector = new InvolvedTypeInspector();
	}

	@Test
	public void testInspect() throws Exception {
		CtClass noSuperClass = pool.get(NoSuperClass.class.getName());
		assertEquals(2, inspector.inspect(noSuperClass).size());
		CtClass subClass = pool.get(SubClass.class.getName());
		assertEquals(6, inspector.inspect(subClass).size());
	}

	@Contract(NoSuperClassContract.class)
	public static class NoSuperClass {
	}

	public static class NoSuperClassContract {
	}

	@Contract(SubClassContract.class)
	public static class SubClass extends SuperClass {
	}

	public static class SubClassContract extends SubClass {
	}
}
