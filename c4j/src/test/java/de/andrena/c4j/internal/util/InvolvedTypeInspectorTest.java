package de.andrena.c4j.internal.util;

import static org.junit.Assert.assertEquals;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.internal.RootTransformerTest.SuperClass;
import de.andrena.c4j.internal.util.InvolvedTypeInspector;

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

	@ContractReference(NoSuperClassContract.class)
	public static class NoSuperClass {
	}

	public static class NoSuperClassContract {
	}

	@ContractReference(SubClassContract.class)
	public static class SubClass extends SuperClass {
	}

	public static class SubClassContract extends SubClass {
	}
}
