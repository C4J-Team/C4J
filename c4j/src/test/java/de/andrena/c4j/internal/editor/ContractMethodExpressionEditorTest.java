package de.andrena.c4j.internal.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.FieldAccess;

import org.junit.Before;
import org.junit.Test;

import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.util.ContractRegistry;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class ContractMethodExpressionEditorTest {

	private ClassPool pool;
	private ContractInfo contract;
	private ContractMethodExpressionEditor editor;
	private FieldAccess fieldAccess;
	private CtClass targetClass;
	private CtClass contractClass;
	private CtClass innerContractClass;

	@Before
	public void before() throws Exception {
		pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
		contract = new ContractRegistry().registerContract(targetClass, contractClass);
		innerContractClass = pool.get(DummyInnerContractClass.class.getName());
		contract.addInnerContractClass(innerContractClass);
		editor = new ContractMethodExpressionEditor(RootTransformer.INSTANCE, contract, contractClass
				.getDeclaredMethod("someMethod"));
		fieldAccess = mock(FieldAccess.class);
		when(fieldAccess.getField()).thenReturn(targetClass.getDeclaredField("someField"));
	}

	@Test
	public void testGetAndClearNestedInnerClasses() {
		editor.nestedInnerClasses.add(targetClass);
		assertEquals(1, editor.getAndClearNestedInnerClasses().size());
	}

	@Test
	public void testGetNestedInnerClassesWhileClearing() {
		editor.nestedInnerClasses.add(targetClass);
		Set<CtClass> nestedInnerClasses = editor.getAndClearNestedInnerClasses();
		assertEquals(1, nestedInnerClasses.size());
	}

	public static class TargetClass {
		protected String someField;

		public void someMethod() {
		}

		public void someMethodWithParameters(int param) {
		}
	}

	public static class ContractClass extends TargetClass {
		protected String contractField;

		@Override
		public void someMethod() {
		}

		public void contractOnlyMethod() {
		}
	}

	public static class DummyInnerContractClass {
		protected String innerContractField;
	}

}
