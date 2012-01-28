package de.andrena.next.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import de.andrena.next.Contract;
import de.andrena.next.internal.transformer.AffectedClassTransformer;
import de.andrena.next.internal.transformer.ContractClassTransformer;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class RootTransformerTest {

	private RootTransformer transformer;
	private AffectedClassTransformer targetClassTransformer;
	private ContractClassTransformer contractClassTransformer;
	private ClassPool pool;
	private CtClass targetClass;
	private CtClass contractClass;

	@Before
	public void before() throws Exception {
		transformer = new RootTransformer("", null);
		targetClassTransformer = mock(AffectedClassTransformer.class);
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
		verify(targetClassTransformer).transform(anySetOf(CtClass.class),
				argThat(new ArgumentMatcher<Set<ContractInfo>>() {
					@Override
					public boolean matches(Object argument) {
						@SuppressWarnings("unchecked")
						Set<ContractInfo> contractInfos = (Set<ContractInfo>) argument;
						return contractInfos != null && contractInfos.size() == 1
								&& contractInfos.iterator().next().getTargetClass().equals(targetClass)
								&& contractInfos.iterator().next().getContractClass().equals(contractClass);
					}
				}), eq(targetClass));
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
		assertNotNull(transformer.transformClass(UninvolvedClass.class.getName()));
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

	@Contract(SuperClassContract.class)
	public static class SuperClass implements HasContract {
	}

	public static class SuperClassContract extends SuperClass {
	}

	@Contract(HasContractContract.class)
	public interface HasContract extends SuperInterface1, SuperInterface2 {
	}

	public static class HasContractContract implements HasContract {
	}

	@Contract(SuperInterface1Contract.class)
	public interface SuperInterface1 {
	}

	public static class SuperInterface1Contract implements SuperInterface1 {
	}

	@Contract(SuperInterface2Contract.class)
	public interface SuperInterface2 {
	}

	public interface SuperInterface2Contract {
	}

}
