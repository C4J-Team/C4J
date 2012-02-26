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

import de.andrena.next.ClassInvariant;
import de.andrena.next.internal.transformer.ContractBehaviorTransformer;

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
