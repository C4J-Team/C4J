package de.andrena.next.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.FieldAccess;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.internal.ContractRegistry.ContractInfo;

public class ContractMethodExpressionEditorTest {

	private ClassPool pool;
	private ContractInfo contract;
	private ContractMethodExpressionEditor editor;

	@Before
	public void before() throws Exception {
		pool = ClassPool.getDefault();
		CtClass targetClass = pool.get(TargetClass.class.getName());
		CtClass contractClass = pool.get(ContractClass.class.getName());
		contract = new ContractRegistry().registerContract(targetClass, contractClass);
		editor = new ContractMethodExpressionEditor(contract, pool);
	}

	@Test
	public void testEditFieldAccess() throws Exception {
		FieldAccess fieldAccess = mock(FieldAccess.class);
		when(fieldAccess.getFieldName()).thenReturn("field");

		editor.editFieldAccess(fieldAccess);
		assertEquals("field", editor.lastFieldAccess);
	}

	@Test
	public void testEditNewExpression() {
	}

	@Test
	public void testEditMethodCall() {
	}

	@Test
	public void testHandleOldMethodCall() {
	}

	@Test
	public void testHandleTargetMethodCall() {
	}

	public static class TargetClass {

	}

	public static class ContractClass {

	}

}
