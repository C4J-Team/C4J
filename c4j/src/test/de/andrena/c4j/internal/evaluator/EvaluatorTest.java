package de.andrena.c4j.internal.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.andrena.c4j.internal.evaluator.Evaluator.EvaluationPhase;
import de.andrena.c4j.internal.util.Pair;

public class EvaluatorTest {

	private static DummyClass currentTarget;

	@BeforeClass
	public static void beforeClass() {
		currentTarget = new DummyClass();
	}

	@Before
	public void before() {
		Evaluator.evaluationPhase.set(EvaluationPhase.NONE);
		Evaluator.currentTarget.set(null);
	}

	@Test
	public void testIsBefore() {
		assertFalse(Evaluator.isBefore());
		Evaluator.evaluationPhase.set(EvaluationPhase.BEFORE);
		assertTrue(Evaluator.isBefore());
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		assertFalse(Evaluator.isBefore());
	}

	@Test
	public void testIsAfter() {
		assertFalse(Evaluator.isAfter());
		Evaluator.evaluationPhase.set(EvaluationPhase.BEFORE);
		assertFalse(Evaluator.isAfter());
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		assertTrue(Evaluator.isAfter());
	}

	@Test
	public void testOldFieldAccess() {
		Evaluator.currentTarget.set(currentTarget);
		Evaluator.currentOldCacheEnvironment.set(new Pair<Integer, Class<?>>(Integer.valueOf(42), ContractClass.class));
		Evaluator.storeFieldAccess("dummyField");
		assertEquals("someValue", Evaluator.oldFieldAccess("dummyField"));
	}

	@Test
	public void testOldMethodCall() {
		Evaluator.currentTarget.set(currentTarget);
		Evaluator.currentOldCacheEnvironment.set(new Pair<Integer, Class<?>>(Integer.valueOf(42), ContractClass.class));
		Evaluator.storeMethodCall("dummyMethod");
		assertEquals("someReturnValue", Evaluator.oldMethodCall("dummyMethod"));
	}

	@Test
	public void testFieldAccess() {
		Evaluator.currentTarget.set(currentTarget);
		assertEquals("someValue", Evaluator.fieldAccess("dummyField"));
	}

	@Test
	public void testMethodCall() {
		Evaluator.currentTarget.set(currentTarget);
		assertEquals(
				7,
				Evaluator.methodCall("dummyMethodWithParameters", new Class[] { int.class, int.class }, new Object[] {
						Integer.valueOf(3), Integer.valueOf(4) }));
	}

	@Test
	public void testPreCondition() {
		Evaluator.beforePre(currentTarget, "SomeClass", ContractClass.class, void.class);
		assertEquals(EvaluationPhase.BEFORE, Evaluator.evaluationPhase.get());
		assertEquals(currentTarget, Evaluator.currentTarget.get());
		Evaluator.afterContract();
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
		assertNull(Evaluator.currentTarget.get());
	}

	public static class ContractClass {
		public void contractMethod() {
		}
	}

	@Test
	public void testPostCondition() {
		Evaluator.beforePost(currentTarget, "SomeClass", ContractClass.class, int.class, Integer.valueOf(4));
		assertEquals(EvaluationPhase.AFTER, Evaluator.evaluationPhase.get());
		assertEquals(currentTarget, Evaluator.currentTarget.get());
		assertEquals(Integer.valueOf(4), Evaluator.returnValue.get());
		Evaluator.afterContract();
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	@Test
	public void testCallInvariant() {
		Evaluator.beforeInvariant(currentTarget, "SomeClass", ContractClass.class);
		assertEquals(EvaluationPhase.INVARIANT, Evaluator.evaluationPhase.get());
		assertEquals(currentTarget, Evaluator.currentTarget.get());
		Evaluator.afterContract();
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	@Test
	public void testGetConditionReturnValue() {
		Evaluator.contractReturnType.set(String.class);
		assertNull(Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(long.class);
		assertEquals(Long.valueOf(0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(int.class);
		assertEquals(Integer.valueOf(0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(short.class);
		assertEquals(Short.valueOf((short) 0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(char.class);
		assertEquals(Character.valueOf((char) 0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(byte.class);
		assertEquals(Byte.valueOf((byte) 0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(double.class);
		assertEquals(Double.valueOf(0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(float.class);
		assertEquals(Float.valueOf(0), Evaluator.getConditionReturnValue());
		Evaluator.contractReturnType.set(boolean.class);
		assertEquals(Boolean.FALSE, Evaluator.getConditionReturnValue());
	}

	public static class DummyClass {
		protected String dummyField = "someValue";

		public String dummyMethod() {
			return "someReturnValue";
		}

		public int dummyMethodWithParameters(int param1, int param2) {
			return param1 + param2;
		}
	}
}
