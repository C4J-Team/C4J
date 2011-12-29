package de.andrena.next.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import de.andrena.next.Contract;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.transformer.ContractClassTransformer;
import de.andrena.next.internal.transformer.TargetClassTransformer;

public class RootTransformerTest {

	private RootTransformer transformer;
	private TargetClassTransformer targetClassTransformer;
	private ContractClassTransformer contractClassTransformer;
	private ClassPool pool;
	private CtClass targetClass;
	private CtClass contractClass;

	@Before
	public void before() throws Exception {
		transformer = new RootTransformer();
		targetClassTransformer = mock(TargetClassTransformer.class);
		transformer.targetClassTransformer = targetClassTransformer;
		contractClassTransformer = mock(ContractClassTransformer.class);
		transformer.contractClassTransformer = contractClassTransformer;
		pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
	}

	@Test
	public void testTransformClassInterface() throws Exception {
		assertNull(transformer.transformClass(EmptyInterface.class.getName()));
	}

	@Test
	public void testTransformClassTargetClass() throws Exception {
		assertNotNull(transformer.transformClass(TargetClass.class.getName()));
		assertEquals(targetClass, transformer.contractRegistry.getContractInfo(contractClass).getTargetClass());
		assertEquals(contractClass, transformer.contractRegistry.getContractInfo(contractClass).getContractClass());
		verify(targetClassTransformer).transform(argThat(new ArgumentMatcher<ContractInfo>() {
			@Override
			public boolean matches(Object argument) {
				ContractInfo contractInfo = (ContractInfo) argument;
				return contractInfo != null && contractInfo.getTargetClass().equals(targetClass)
						&& contractInfo.getContractClass().equals(contractClass);
			}
		}));
	}

	@Test
	public void testTransformClassContractClass() throws Exception {
		transformer.contractRegistry.registerContract(targetClass, contractClass);
		assertNotNull(transformer.transformClass(ContractClass.class.getName()));
		verify(contractClassTransformer).transform(argThat(new ArgumentMatcher<ContractInfo>() {
			@Override
			public boolean matches(Object argument) {
				ContractInfo contractInfo = (ContractInfo) argument;
				return contractInfo != null && contractInfo.getTargetClass().equals(targetClass)
						&& contractInfo.getContractClass().equals(contractClass);
			}
		}), eq(contractClass));
	}

	@Test
	public void testTransformClassUninvolvedClass() throws Exception {
		assertNull(transformer.transformClass(UninvolvedClass.class.getName()));
	}

	@Test
	public void testUpdateClassPathWithClassLoader() {
		ClassPool poolMock = mock(ClassPool.class);
		transformer.pool = poolMock;
		ClassLoader classLoader = mock(ClassLoader.class);
		transformer.updateClassPath(classLoader, null, null);
		verify(poolMock).insertClassPath(any(LoaderClassPath.class));
	}

	@Test
	public void testUpdateClassPathWithByteArray() {
		ClassPool poolMock = mock(ClassPool.class);
		transformer.pool = poolMock;
		ClassLoader classLoader = mock(ClassLoader.class);
		transformer.updateClassPath(classLoader, null, null);
		verify(poolMock).insertClassPath(any(ByteArrayClassPath.class));
	}

	public interface EmptyInterface {
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
	}

	public static class ContractClass extends TargetClass {
	}

	public static class UninvolvedClass {
	}
}
