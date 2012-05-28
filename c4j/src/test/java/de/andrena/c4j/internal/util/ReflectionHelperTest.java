package de.andrena.c4j.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.internal.transformer.ContractBehaviorTransformer;

public class ReflectionHelperTest {
	private ReflectionHelper helper;
	private ClassPool pool;
	private CtClass contractClass;

	@Before
	public void before() throws Throwable {
		helper = new ReflectionHelper();
		pool = ClassPool.getDefault();
		contractClass = pool.get(ContractClass.class.getName());
	}

	@Test
	public void testIsModifiable() throws Throwable {
		CtClass objectClass = pool.get(Object.class.getName());
		assertTrue(ReflectionHelper.isModifiable(objectClass.getDeclaredMethod("finalize")));
		assertFalse(ReflectionHelper.isModifiable(objectClass.getDeclaredMethod("hashCode")));
	}

	@Test
	public void testIsDynamic() throws Throwable {
		assertTrue(ReflectionHelper.isDynamic(pool.get(Object.class.getName()).getDeclaredMethod("finalize")));
		assertFalse(ReflectionHelper.isDynamic(pool.get(Arrays.class.getName()).getDeclaredMethod("asList")));
	}

	@Test
	public void testGetContractBehaviorNameForMethod() throws Exception {
		assertEquals(helper.getContractBehaviorName(contractClass.getDeclaredMethod("contractMethod")),
				"contractMethod");
	}

	@Test
	public void testGetContractBehaviorNameForConstructor() throws Exception {
		assertEquals(helper.getContractBehaviorName(contractClass.getDeclaredConstructor(new CtClass[0])),
				ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	@Test
	public void testGetContractBehaviorNameForTransformedConstructor() throws Exception {
		assertEquals(helper.getContractBehaviorName(contractClass
				.getDeclaredMethod(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME)),
				ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	@Test
	public void testIsConstructorForMethod() throws Exception {
		assertFalse(helper.isContractConstructor(contractClass.getDeclaredMethod("contractMethod")));
	}

	@Test
	public void testIsConstructorForConstructor() throws Exception {
		assertTrue(helper.isContractConstructor(contractClass.getDeclaredConstructor(new CtClass[0])));
	}

	@Test
	public void testIsConstructorForTransformedConstructor() throws Exception {
		assertTrue(helper.isContractConstructor(contractClass
				.getDeclaredMethod(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME)));
	}

	public static class TargetClass {
		public TargetClass() {
		}

		public TargetClass(double value) {
		}

		public void contractMethod() {
		}
	}

	public static class ContractClass extends TargetClass {
		public ContractClass() {
		}

		public ContractClass(int value) {
		}

		public ContractClass(double value) {
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
