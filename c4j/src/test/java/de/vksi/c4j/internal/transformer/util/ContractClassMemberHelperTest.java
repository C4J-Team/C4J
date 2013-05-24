package de.vksi.c4j.internal.transformer.util;

import static de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper.isContractConstructor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ConstructorContract;

public class ContractClassMemberHelperTest {

	private CtClass contractClass;
	private ClassPool pool;

	@Before
	public void before() throws Throwable {
		pool = ClassPool.getDefault();
		contractClass = pool.get(ContractClass.class.getName());
	}

	@Test
	public void testIsConstructorForMethod() throws Exception {
		assertFalse(isContractConstructor(contractClass.getDeclaredMethod("contractMethod")));
	}

	@Test
	public void testIsConstructorForConstructor() throws Exception {
		assertTrue(isContractConstructor(contractClass.getDeclaredMethod("constructor")));
	}

	@SuppressWarnings("unused")
	private static class TargetClass {
		public TargetClass() {
		}

		public TargetClass(double value) {
		}

		public void contractMethod() {
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass extends TargetClass {
		public ContractClass() {
		}

		@ConstructorContract
		public void constructor() {
		}

		@ConstructorContract
		public void constructor(int value) {
		}

		@ConstructorContract
		public void constructor(double value) {
		}

		@ClassInvariant
		public void invariant() {
		}

		public void constructor$() {
		}

		@Override
		public void contractMethod() {
		}

		public void otherMethod() {
		}
	}
}
