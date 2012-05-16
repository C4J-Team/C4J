package de.andrena.c4j.internal;

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

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.internal.transformer.AffectedClassTransformer;
import de.andrena.c4j.internal.transformer.ContractClassTransformer;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.ListOrderedSet;

public class RootTransformerTest {

	private RootTransformer transformer;
	private AffectedClassTransformer targetClassTransformer;
	private ContractClassTransformer contractClassTransformer;
	private ClassPool pool;
	private CtClass targetClass;
	private CtClass contractClass;

	@Before
	public void before() throws Exception {
		transformer = RootTransformer.INSTANCE;
		transformer.init();
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
		assertNull(transformer.transformType(pool.get(EmptyInterface.class.getName())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTransformClassTargetClass() throws Exception {
		assertNotNull(transformer.transformType(pool.get(TargetClass.class.getName())));
		assertEquals(targetClass, transformer.contractRegistry.getContractInfo(contractClass).getTargetClass());
		assertEquals(contractClass, transformer.contractRegistry.getContractInfo(contractClass).getContractClass());
		verify(targetClassTransformer).transform(any(ListOrderedSet.class),
				argThat(new ArgumentMatcher<ListOrderedSet<ContractInfo>>() {
					@Override
					public boolean matches(Object argument) {
						ListOrderedSet<ContractInfo> contractInfos = (ListOrderedSet<ContractInfo>) argument;
						return contractInfos != null && contractInfos.size() == 1
								&& contractInfos.iterator().next().getTargetClass().equals(targetClass)
								&& contractInfos.iterator().next().getContractClass().equals(contractClass);
					}
				}), eq(targetClass));
	}

	@Test
	public void testTransformClassContractClass() throws Exception {
		transformer.contractRegistry.registerContract(targetClass, contractClass);
		assertNotNull(transformer.transformType(pool.get(ContractClass.class.getName())));
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
		assertNotNull(transformer.transformType(pool.get(UninvolvedClass.class.getName())));
	}

	@Test
	public void testUpdateClassPathWithClassLoader() {
		ClassPool poolMock = mock(ClassPool.class);
		ClassPool oldPool = transformer.pool;
		transformer.pool = poolMock;
		ClassLoader classLoader = mock(ClassLoader.class);
		transformer.updateClassPath(classLoader, null, null);
		transformer.pool = oldPool;
		verify(poolMock).insertClassPath(any(LoaderClassPath.class));
	}

	@Test
	public void testUpdateClassPathWithByteArray() {
		ClassPool poolMock = mock(ClassPool.class);
		ClassPool oldPool = transformer.pool;
		transformer.pool = poolMock;
		ClassLoader classLoader = mock(ClassLoader.class);
		transformer.updateClassPath(classLoader, null, null);
		transformer.pool = oldPool;
		verify(poolMock).insertClassPath(any(ByteArrayClassPath.class));
	}

	public interface EmptyInterface {
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
	}

	public static class ContractClass extends TargetClass {
	}

	public static class UninvolvedClass {
	}

	@ContractReference(SuperClassContract.class)
	public static class SuperClass implements HasContract {
	}

	public static class SuperClassContract extends SuperClass {
	}

	@ContractReference(HasContractContract.class)
	public interface HasContract extends SuperInterface1, SuperInterface2 {
	}

	public static class HasContractContract implements HasContract {
	}

	@ContractReference(SuperInterface1Contract.class)
	public interface SuperInterface1 {
	}

	public static class SuperInterface1Contract implements SuperInterface1 {
	}

	@ContractReference(SuperInterface2Contract.class)
	public interface SuperInterface2 {
	}

	public interface SuperInterface2Contract {
	}

}
