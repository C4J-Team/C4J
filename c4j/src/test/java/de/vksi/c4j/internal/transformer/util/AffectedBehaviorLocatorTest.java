package de.vksi.c4j.internal.transformer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.junit.Before;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ConstructorContract;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;

public class AffectedBehaviorLocatorTest {
	private AffectedBehaviorLocator locator;

	private CtClass contractClass;
	private CtClass targetClass;
	private ContractInfo contractInfo;
	private CtClass indirectClass;
	private CtClass targetInterface;
	private CtClass contractClassForTargetInterface;
	private ContractInfo contractInfoForTargetInterface;

	@Before
	public void before() throws Exception {
		locator = new AffectedBehaviorLocator();
		ClassPool pool = ClassPool.getDefault();
		contractClass = pool.get(ContractClass.class.getName());
		targetClass = pool.get(TargetClass.class.getName());
		indirectClass = pool.get(IndirectClass.class.getName());
		contractInfo = ContractRegistry.INSTANCE.registerContract(targetClass, contractClass);
		targetInterface = pool.get(TargetInterface.class.getName());
		contractClassForTargetInterface = pool.get(TargetInterfaceContract.class.getName());
		contractInfoForTargetInterface = ContractRegistry.INSTANCE.registerContract(targetInterface,
				contractClassForTargetInterface);
	}

	@Test
	public void testGetAffectedBehaviorForClassInvariant() throws Exception {
		assertNull(locator.getAffectedBehavior(null, null, contractClass.getDeclaredMethod("invariant")));
	}

	@Test(expected = NotFoundException.class)
	public void testGetAffectedBehaviorForNonMethodOrConstructor() throws Exception {
		CtBehavior contractBehavior = mock(CtBehavior.class);
		when(contractBehavior.getName()).thenReturn("contractBehavior");
		locator.getAffectedBehavior(null, null, contractBehavior);
	}

	@Test
	public void testGetAffectedMethodForOtherMethod() throws Exception {
		assertNull(locator.getAffectedMethod(contractInfo, targetClass, contractClass.getDeclaredMethod("otherMethod")));
	}

	@Test
	public void testGetAffectedMethodForContractMethod() throws Exception {
		assertEquals(targetClass.getDeclaredMethod("contractMethod"), locator.getAffectedMethod(contractInfo,
				targetClass, contractClass.getDeclaredMethod("contractMethod")));
	}

	@Test
	public void testGetAffectedMethodForIndirectContractMethod() throws Exception {
		RootTransformer.INSTANCE.init();
		CtMethod affectedMethod = locator.getAffectedMethod(contractInfo, indirectClass, contractClass
				.getDeclaredMethod("contractMethod"));
		assertEquals(indirectClass.getDeclaredMethod("contractMethod"), affectedMethod);
	}

	@Test
	public void testGetAffectedConstructor() throws Exception {
		assertEquals(targetClass.getDeclaredConstructor(new CtClass[] { CtClass.doubleType }), locator
				.getAffectedConstructor(contractInfo, targetClass, contractClass.getDeclaredMethod("constructor",
						new CtClass[] { CtClass.doubleType })));
	}

	@Test
	public void testGetAffectedConstructorForSynthetic() throws Exception {
		assertEquals(targetClass.getDeclaredConstructor(new CtClass[0]), locator.getAffectedConstructor(contractInfo,
				targetClass, contractClass.getDeclaredMethod("constructor", new CtClass[0])));
	}

	@Test
	public void testGetAffectedConstructorNotFound() throws Exception {
		assertNull(locator.getAffectedConstructor(contractInfo, targetClass, contractClass.getDeclaredMethod(
				"constructor", new CtClass[] { CtClass.intType })));
	}

	@Test
	public void testGetAffectedConstructorForTargetInterface() throws Exception {
		assertNull(locator.getAffectedConstructor(contractInfoForTargetInterface, targetInterface, contractClass
				.getDeclaredConstructor(new CtClass[0])));
	}

	private static class IndirectClass extends TargetClass {
	}

	private static class TargetClass {
		public TargetClass() {
		}

		public TargetClass(double value) {
		}

		public void contractMethod() {
		}
	}

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

		@Override
		public void contractMethod() {
		}

		public void otherMethod() {
		}
	}

	@ContractReference(TargetInterfaceContract.class)
	public interface TargetInterface {
	}

	private static class TargetInterfaceContract implements TargetInterface {
	}
}
