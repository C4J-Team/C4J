package de.andrena.next.internal.transformer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.ClassInvariant;
import de.andrena.next.Contract;
import de.andrena.next.internal.ContractRegistry;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public class ClassInvariantTransformerTest {

	private ClassInvariantTransformer transformer;
	private ClassPool pool;
	private CtClass targetClass;
	private CtClass contractClass;
	private ContractInfo contractInfo;
	private CtMethod targetMethod;

	@Before
	public void before() throws Exception {
		transformer = new ClassInvariantTransformer();
		pool = ClassPool.getDefault();
		targetClass = mock(CtClass.class);
		contractClass = pool.get(ContractClass.class.getName());
		targetMethod = mock(CtMethod.class);
		CtMethod[] targetBehaviors = new CtMethod[] { targetMethod };
		when(targetClass.getDeclaredBehaviors()).thenReturn(targetBehaviors);
		contractInfo = new ContractRegistry().registerContract(targetClass, contractClass);
	}

	@Test
	public void testClassInvariant() throws Exception {
		when(targetMethod.hasAnnotation(ClassInvariant.class)).thenReturn(Boolean.TRUE);
		transformer.transform(contractInfo);
		verify(targetMethod).insertAfter(anyString());
	}

	@Contract(ContractClass.class)
	public static class TargetClass {

	}

	public static class ContractClass extends TargetClass {
		@ClassInvariant
		public void invariant() {
		}
	}
}
