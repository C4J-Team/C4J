package de.andrena.next.internal.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.internal.ContractRegistry;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeforeAndAfterTriggerTransformerTest {

	private BeforeAndAfterTriggerTransformer transformer;
	private CtClass targetClass;
	private CtClass contractClass;
	private ContractInfo contractInfo;
	private CtMethod targetMethod;

	@Before
	public void before() throws Exception {
		transformer = new BeforeAndAfterTriggerTransformer();
		ClassPool pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
		contractInfo = new ContractRegistry().registerContract(targetClass, contractClass);
		targetMethod = mock(CtMethod.class);
	}

	@Test
	public void testTransformNothing() throws Exception {
		when(targetMethod.getName()).thenReturn("methodWithoutContract");
		when(targetMethod.getParameterTypes()).thenReturn(new CtClass[0]);
		transformer.transform(contractInfo, targetMethod);
		verify(targetMethod, never()).insertBefore(anyString());
		verify(targetMethod, never()).insertAfter(anyString());
	}

	@Test
	public void testTransformSomething() throws Exception {
		when(targetMethod.getName()).thenReturn("methodWithContract");
		when(targetMethod.getParameterTypes()).thenReturn(new CtClass[0]);
		when(targetMethod.getReturnType()).thenReturn(CtClass.voidType);
		transformer.transform(contractInfo, targetMethod);
		verify(targetMethod).insertBefore(anyString());
		verify(targetMethod).insertAfter(anyString());
	}

	public static class TargetClass {
		public void methodWithoutContract() {
		}

		public void methodWithContract() {
		}
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void methodWithContract() {
		}
	}
}
