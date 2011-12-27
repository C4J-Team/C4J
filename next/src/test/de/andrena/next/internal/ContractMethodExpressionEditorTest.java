package de.andrena.next.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.Condition;
import de.andrena.next.Condition.PostCondition;
import de.andrena.next.Condition.PreCondition;
import de.andrena.next.internal.ContractRegistry.ContractInfo;

public class ContractMethodExpressionEditorTest {

	private ClassPool pool;
	private ContractInfo contract;
	private ContractMethodExpressionEditor editor;
	private FieldAccess fieldAccess;
	private NewExpr newExpr;
	private MethodCall methodCall;
	private CtClass targetClass;
	private CtClass contractClass;
	private CtMethod oldMethod;
	private CtClass innerPreConditionClass;
	private CtClass innerPostConditionClass;

	@Before
	public void before() throws Exception {
		pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
		contract = new ContractRegistry().registerContract(targetClass, contractClass);
		innerPreConditionClass = pool.get(DummyPreCondition.class.getName());
		contract.addInnerContractClass(innerPreConditionClass);
		innerPostConditionClass = pool.get(DummyPostCondition.class.getName());
		contract.addInnerContractClass(innerPostConditionClass);
		editor = new ContractMethodExpressionEditor(contract, pool);
		fieldAccess = mock(FieldAccess.class);
		when(fieldAccess.getField()).thenReturn(targetClass.getDeclaredField("someField"));
		newExpr = mock(NewExpr.class);
		methodCall = mock(MethodCall.class);
		oldMethod = pool.get(Condition.class.getName()).getDeclaredMethod("old");
	}

	@Test
	public void testEditFieldAccess() throws Exception {
		editor.editFieldAccess(fieldAccess);
		assertEquals(targetClass.getDeclaredField("someField"), editor.lastFieldAccess);
		assertNull(editor.lastMethodCall);
	}

	@Test
	public void testEditFieldAccessOnTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.FALSE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.FALSE);
		editor.editFieldAccess(fieldAccess);
		verify(fieldAccess).replace(contains("someField"));
	}

	@Test
	public void testEditFieldAccessOnStaticTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.TRUE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.FALSE);
		editor.editFieldAccess(fieldAccess);
		verify(fieldAccess, never()).replace(anyString());
	}

	@Test(expected = TransformationException.class)
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
		when(fieldAccess.getField()).thenReturn(innerPreConditionClass.getDeclaredField("innerContractField"));
		when(fieldAccess.isStatic()).thenReturn(Boolean.FALSE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.TRUE);
		editor.editFieldAccess(fieldAccess);
	}

	@Test(expected = TransformationException.class)
	public void testEditFieldAccessOnWrittenStaticTargetField() throws Exception {
		when(fieldAccess.isStatic()).thenReturn(Boolean.TRUE);
		when(fieldAccess.isWriter()).thenReturn(Boolean.TRUE);
		editor.editFieldAccess(fieldAccess);
	}

	@Test
	public void testEditNewExpressionPreCondition() throws Exception {
		CtClass preConditionClass = pool.get(DummyPreCondition.class.getName());
		when(newExpr.getClassName()).thenReturn(DummyPreCondition.class.getName());
		editor.editNewExpression(newExpr);
		verify(newExpr).replace(anyString());
		assertTrue(contract.getInnerContractClasses().contains(preConditionClass));
		assertTrue(editor.getNestedInnerClasses().contains(preConditionClass));
	}

	@Test
	public void testEditNewExpressionPostCondition() throws Exception {
		CtClass postConditionClass = pool.get(DummyPostCondition.class.getName());
		when(newExpr.getClassName()).thenReturn(DummyPostCondition.class.getName());
		editor.editNewExpression(newExpr);
		verify(newExpr).replace(anyString());
		assertTrue(contract.getInnerContractClasses().contains(postConditionClass));
		assertTrue(editor.getNestedInnerClasses().contains(postConditionClass));
	}

	@Test
	public void testEditMethodCallToTargetMethod() throws Exception {
		when(methodCall.getMethod()).thenReturn(targetClass.getDeclaredMethod("someMethod"));
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(targetClass.getDeclaredMethod("someMethod"), editor.lastMethodCall);
		assertNull(editor.lastFieldAccess);
	}

	@Test
	public void testEditMethodCallToOverriddenContractMethod() throws Exception {
		when(methodCall.getMethod()).thenReturn(contractClass.getDeclaredMethod("someMethod"));
		editor.editMethodCall(methodCall);
		verify(methodCall).replace(anyString());
		assertEquals(targetClass.getDeclaredMethod("someMethod"), editor.lastMethodCall);
		assertNull(editor.lastFieldAccess);
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

	@Test(expected = TransformationException.class)
	public void testEditMethodCallToOldWithMethodAndParameters() throws Exception {
		when(methodCall.getMethod()).thenReturn(oldMethod);
		editor.lastMethodCall = targetClass.getDeclaredMethod("someMethodWithParameters");
		editor.editMethodCall(methodCall);
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
	}

	public static class DummyPreCondition implements PreCondition {
		protected String innerContractField;
	}

	public static class DummyPostCondition implements PostCondition {
	}

}
