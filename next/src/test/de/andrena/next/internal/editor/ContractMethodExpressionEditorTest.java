package de.andrena.next.internal.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.util.ContractRegistry;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

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
	private CtMethod unchangedMethod;

	@Before
	public void before() throws Exception {
		pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
		contract = new ContractRegistry().registerContract(targetClass, contractClass);
		innerContractClass = pool.get(DummyInnerContractClass.class.getName());
		contract.addInnerContractClass(innerContractClass);
		editor = new ContractMethodExpressionEditor(RootTransformer.INSTANCE, contract);
		fieldAccess = mock(FieldAccess.class);
		when(fieldAccess.getField()).thenReturn(targetClass.getDeclaredField("someField"));
		methodCall = mock(MethodCall.class);
		CtClass conditionClass = pool.get(Condition.class.getName());
		oldMethod = conditionClass.getDeclaredMethod("old");
		unchangedMethod = conditionClass.getDeclaredMethod("unchanged");
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
	public void testEditFieldAccess() throws Exception {
		editor.editFieldAccess(fieldAccess);
		assertEquals(targetClass.getDeclaredField("someField"), editor.lastFieldAccess);
		assertNull(editor.lastMethodCall);
	}

	@Test
	public void testEditFieldAccessOnStaticTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.TRUE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.FALSE);
		editor.editFieldAccess(fieldAccess);
		verify(fieldAccess, never()).replace(anyString());
	}

	@Test(expected = CannotCompileException.class)
	public void testEditFieldAccessOnWrittenTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.FALSE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.TRUE);
		editor.editFieldAccess(fieldAccess);
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

	@Test(expected = CannotCompileException.class)
	public void testEditFieldAccessOnWrittenStaticTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.TRUE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.TRUE);
		editor.editFieldAccess(fieldAccess);
	}

	@Test
	public void testEditMethodCallToOldWithField() throws Exception {
		when(methodCall.getMethod()).thenReturn(oldMethod);
		editor.lastFieldAccess = targetClass.getDeclaredField("someField");
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(1, editor.getStoreExpressions().size());
	}

	@Test
	public void testEditMethodCallToOldWithMethod() throws Exception {
		when(methodCall.getMethod()).thenReturn(oldMethod);
		editor.lastMethodCall = targetClass.getDeclaredMethod("someMethod");
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(1, editor.getStoreExpressions().size());
	}

	@Test
	public void testEditMethodCallToOldWithOverriddenMethod() throws Exception {
		when(methodCall.getMethod()).thenReturn(oldMethod);
		editor.lastMethodCall = contractClass.getDeclaredMethod("someMethod");
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(1, editor.getStoreExpressions().size());
	}

	@Test(expected = CannotCompileException.class)
	public void testEditMethodCallToOldWithMethodAndParameters() throws Exception {
		when(methodCall.getMethod()).thenReturn(oldMethod);
		editor.lastMethodCall = targetClass.getDeclaredMethod("someMethodWithParameters");
		editor.editMethodCall(methodCall);
	}

	@Test
	public void testEditMethodCallToUnchangedWithField() throws Exception {
		when(methodCall.getMethod()).thenReturn(unchangedMethod);
		editor.arrayMembers.add(targetClass.getDeclaredField("someField"));
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(1, editor.getStoreExpressions().size());
	}

	@Test
	public void testEditMethodCallToUnchangedWithMethod() throws Exception {
		when(methodCall.getMethod()).thenReturn(unchangedMethod);
		editor.arrayMembers.add(targetClass.getDeclaredMethod("someMethod"));
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(1, editor.getStoreExpressions().size());
	}

	@Test
	public void testEditMethodCallToUnchangedWithFieldAndMethod() throws Exception {
		when(methodCall.getMethod()).thenReturn(unchangedMethod);
		editor.arrayMembers.add(targetClass.getDeclaredField("someField"));
		editor.arrayMembers.add(targetClass.getDeclaredMethod("someMethod"));
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(2, editor.getStoreExpressions().size());
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
