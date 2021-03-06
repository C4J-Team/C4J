package de.vksi.c4j.internal.contracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

public class ContractRegistryTest {

	private ContractRegistry contractRegistry;
	private ClassPool pool;
	private CtClass targetClass;
	private CtClass contractClass;

	@Before
	public void before() throws Exception {
		contractRegistry = new ContractRegistry();
		pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
	}

	@Test
	public void testRegisterContract() {
		ContractInfo contractInfo = contractRegistry.registerContract(targetClass, contractClass);
		assertEquals(targetClass, contractInfo.getTargetClass());
		assertEquals(contractClass, contractInfo.getContractClass());
		assertTrue(contractInfo.getInnerContractClasses().isEmpty());
		assertEquals(1, contractInfo.getAllContractClasses().size());
		assertTrue(contractInfo.getAllContractClasses().contains(contractClass));
	}

	@Test
	public void testRegisterContractForAlreadyRegisteredContract() {
		ContractInfo contractInfo = contractRegistry.registerContract(targetClass, contractClass);
		assertEquals(contractInfo, contractRegistry.registerContract(targetClass, contractClass));
	}

	@Test
	public void testIsContractClass() {
		assertFalse(contractRegistry.isContractClass(contractClass));
		contractRegistry.registerContract(targetClass, contractClass);
		assertTrue(contractRegistry.isContractClass(contractClass));
	}

	@Test
	public void testGetContractInfo() {
		assertNull(contractRegistry.getContractInfo(contractClass));
		ContractInfo contractInfo = contractRegistry.registerContract(targetClass, contractClass);
		assertEquals(contractInfo, contractRegistry.getContractInfo(contractClass));
	}

	@Test
	public void testHasRegisteredContract() {
		assertFalse(contractRegistry.hasRegisteredContract(targetClass));
		contractRegistry.registerContract(targetClass, contractClass);
		assertTrue(contractRegistry.hasRegisteredContract(targetClass));
	}

	@Test
	public void testGetContractInfoForTargetClass() {
		assertNull(contractRegistry.getContractInfoForTargetClass(targetClass));
		ContractInfo contractInfo = contractRegistry.registerContract(targetClass, contractClass);
		assertEquals(contractInfo, contractRegistry.getContractInfoForTargetClass(targetClass));
	}

	@Test
	public void testGetAllContractClasses() {
		ContractInfo contractInfo = contractRegistry.registerContract(targetClass, contractClass);
		assertEquals(1, contractInfo.getAllContractClasses().size());
		assertTrue(contractInfo.getAllContractClasses().contains(contractClass));
	}

	private static class TargetClass {
	}

	private static class ContractClass extends TargetClass {
	}

}
