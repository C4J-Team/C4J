package de.andrena.c4j.internal.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.junit.Before;
import org.junit.Test;

import de.andrena.c4j.Condition;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.util.ContractRegistry;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class ContractMethodExpressionEditorTest {

	private ClassPool pool;
	private ContractInfo contract;
	private ContractMethodExpressionEditor editor;
	private FieldAccess fieldAccess;
	private MethodCall methodCall;
	private CtClass targetClass;
	private CtClass contractClass;
	private CtMethod oldMethod;
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
		methodCall = mock(MethodCall.class);
		CtClass conditionClass = pool.get(Condition.class.getName());
		oldMethod = conditionClass.getDeclaredMethod("old");
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

	@Test
	public void testEditFieldAccessOnStaticTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.TRUE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.FALSE);
		editor.editFieldAccess(fieldAccess);
		verify(fieldAccess, never()).replace(anyString());
	}

	@Test
	public void testEditFieldAccessOnWrittenContractField() throws Exception {
		when(fieldAccess.getField()).thenReturn(contractClass.getDeclaredField("contractField"));
		when(fieldAccess.isStatic()).thenReturn(Boolean.FALSE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.TRUE);
		editor.editFieldAccess(fieldAccess);
	}

	@Test
	public void testEditFieldAccessOnWrittenInnerContractField() throws Exception {
		when(fieldAccess.getField()).thenReturn(innerContractClass.getDeclaredField("innerContractField"));
		when(fieldAccess.isStatic()).thenReturn(Boolean.FALSE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.TRUE);
		editor.editFieldAccess(fieldAccess);
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
