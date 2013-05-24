package de.vksi.c4j.internal.contracts;

import static org.junit.Assert.assertEquals;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import de.vksi.c4j.ContractReference;

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
		assertEquals(3, inspector.inspect(subClass).size());
	}

	@ContractReference(NoSuperClassContract.class)
	private static class NoSuperClass {
	}

	private static class NoSuperClassContract {
	}

	private static class SuperClass {
	}

	@ContractReference(SubClassContract.class)
	private static class SubClass extends SuperClass {
	}

	private static class SubClassContract extends SubClass {
	}
}
