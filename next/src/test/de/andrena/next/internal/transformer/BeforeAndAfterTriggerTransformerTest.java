package de.andrena.next.internal.transformer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.internal.ContractRegistry;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public class BeforeAndAfterTriggerTransformerTest {

	private BeforeAndAfterTriggerTransformer transformer;
	private CtClass targetClass;
	private CtClass contractClass;
	private ContractInfo contractInfo;
	private CtBehavior targetBehavior;

	@Before
	public void before() throws Exception {
		transformer = new BeforeAndAfterTriggerTransformer();
		ClassPool pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
		contractInfo = new ContractRegistry().registerContract(targetClass, contractClass);
	}

	@Test
	public void testTransformNothing() throws Exception {
		targetBehavior = mock(CtMethod.class);
		when(targetBehavior.getName()).thenReturn("methodWithoutContract");
		when(targetBehavior.getParameterTypes()).thenReturn(new CtClass[0]);
		transformer.transform(contractInfo, targetBehavior);
		verify(targetBehavior, never()).insertBefore(anyString());
		verify(targetBehavior, never()).insertAfter(anyString());
	}

	@Test
	public void testTransformSomething() throws Exception {
		targetBehavior = mock(CtMethod.class);
		when(targetBehavior.getName()).thenReturn("methodWithContract");
		when(targetBehavior.getParameterTypes()).thenReturn(new CtClass[0]);
		when(((CtMethod) targetBehavior).getReturnType()).thenReturn(CtClass.voidType);
		transformer.transform(contractInfo, targetBehavior);
		verify(targetBehavior).insertBefore(anyString());
		verify(targetBehavior).insertAfter(anyString());
	}

	@Test
	public void testTransformConstructor() throws Exception {
		targetBehavior = mock(CtConstructor.class);
		when(targetBehavior.getName()).thenReturn("BeforeAndAfterTriggerTransformerTest$ContractClass");
		when(targetBehavior.getParameterTypes()).thenReturn(new CtClass[] { CtClass.intType });
		transformer.transform(contractInfo, targetBehavior);
		verify(((CtConstructor) targetBehavior)).insertBeforeBody(anyString());
		verify(targetBehavior).insertAfter(anyString());
	}

	public static class TargetClass {
		public TargetClass(int value) {
		}

		public void methodWithoutContract() {
		}

		public void methodWithContract() {
		}
	}

	public static class ContractClass extends TargetClass {
		public ContractClass(int value) {
			super(value);
		}

		@Override
		public void methodWithContract() {
		}
	}
}
