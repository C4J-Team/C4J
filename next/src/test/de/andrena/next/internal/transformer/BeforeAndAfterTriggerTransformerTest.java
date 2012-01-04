package de.andrena.next.internal.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.ClassInvariant;
import de.andrena.next.internal.util.ContractRegistry;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class BeforeAndAfterTriggerTransformerTest {

	private BeforeAndAfterTriggerTransformer transformer;
	private CtClass contractClass;
	private CtClass targetClass;
	private ContractInfo contractInfo;
	private CtClass indirectClass;

	@Before
	public void before() throws Exception {
		transformer = new BeforeAndAfterTriggerTransformer();
		ClassPool pool = ClassPool.getDefault();
		contractClass = pool.get(ContractClass.class.getName());
		targetClass = pool.get(TargetClass.class.getName());
		indirectClass = pool.get(IndirectClass.class.getName());
		contractInfo = new ContractRegistry().registerContract(targetClass, contractClass);
	}

	@Test
	public void testGetContractBehaviorNameForMethod() throws Exception {
		assertEquals(transformer.getContractBehaviorName(contractClass.getDeclaredMethod("contractMethod")),
				"contractMethod");
	}

	@Test
	public void testGetContractBehaviorNameForConstructor() throws Exception {
		assertEquals(transformer.getContractBehaviorName(contractClass.getDeclaredConstructor(new CtClass[0])),
				ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	@Test
	public void testGetContractBehaviorNameForTransformedConstructor() throws Exception {
		assertEquals(transformer.getContractBehaviorName(contractClass
				.getDeclaredMethod(ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME)),
				ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME);
	}

	public static class IndirectClass extends TargetClass {
	}

	public static class TargetClass {
		public void contractMethod() {
		}
	}

	public static class ContractClass extends TargetClass {
		public ContractClass() {
		}

		public ContractClass(int value) {
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

	@Test
	public void testGetAffectedBehaviorForClassInvariant() throws Exception {
		assertNull(transformer.getAffectedBehavior(null, null, contractClass.getDeclaredMethod("invariant")));
	}

	@Test(expected = TransformationException.class)
	public void testGetAffectedBehaviorForNonMethodOrConstructor() throws Exception {
		CtBehavior contractBehavior = mock(CtBehavior.class);
		when(contractBehavior.getName()).thenReturn("contractBehavior");
		transformer.getAffectedBehavior(null, null, contractBehavior);
	}

	@Test
	public void testGetAffectedMethodForOtherMethod() throws Exception {
		assertNull(transformer.getAffectedMethod(contractInfo, targetClass,
				contractClass.getDeclaredMethod("otherMethod")));
	}

	@Test
	public void testGetAffectedMethodForContractMethod() throws Exception {
		assertEquals(
				targetClass.getDeclaredMethod("contractMethod"),
				transformer.getAffectedMethod(contractInfo, targetClass,
						contractClass.getDeclaredMethod("contractMethod")));
	}

	@Test
	public void testGetAffectedMethodForIndirectContractMethod() throws Exception {
		CtMethod affectedMethod = transformer.getAffectedMethod(contractInfo, indirectClass,
				contractClass.getDeclaredMethod("contractMethod"));
		assertEquals(indirectClass.getDeclaredMethod("contractMethod"), affectedMethod);
	}

	@Test
	public void testGetAffectedConstructor() throws Exception {
		assertEquals(
				targetClass.getDeclaredConstructor(new CtClass[0]),
				transformer.getAffectedConstructor(contractInfo, targetClass,
						contractClass.getDeclaredConstructor(new CtClass[0])));
	}

	@Test
	public void testGetAffectedConstructorNotFound() throws Exception {
		assertNull(transformer.getAffectedConstructor(contractInfo, targetClass,
				contractClass.getDeclaredConstructor(new CtClass[] { CtClass.intType })));
	}

	@Test
	public void testIsConstructorForMethod() throws Exception {
		assertFalse(transformer.isConstructor(contractClass.getDeclaredMethod("contractMethod")));
	}

	@Test
	public void testIsConstructorForConstructor() throws Exception {
		assertTrue(transformer.isConstructor(contractClass.getDeclaredConstructor(new CtClass[0])));
	}

	@Test
	public void testIsConstructorForTransformedConstructor() throws Exception {
		assertTrue(transformer.isConstructor(contractClass
				.getDeclaredMethod(ConstructorTransformer.CONSTRUCTOR_REPLACEMENT_NAME)));
	}

}
