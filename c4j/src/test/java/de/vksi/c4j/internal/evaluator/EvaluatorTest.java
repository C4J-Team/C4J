package de.vksi.c4j.internal.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.evaluator.Evaluator.EvaluationPhase;

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
	public void testPreCondition() throws Throwable {
		Evaluator.getPreCondition(currentTarget, "SomeClass", ContractClass.class, DummyClass.class, void.class);
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
	public void testPostCondition() throws Throwable {
		Evaluator.getPostCondition(currentTarget, "SomeClass", ContractClass.class, DummyClass.class, int.class,
				Integer.valueOf(4));
		assertEquals(EvaluationPhase.AFTER, Evaluator.evaluationPhase.get());
		assertEquals(currentTarget, Evaluator.currentTarget.get());
		assertEquals(Integer.valueOf(4), Evaluator.returnValue.get());
		Evaluator.afterContract();
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	@Test
	public void testCallInvariant() throws Throwable {
		Evaluator.getInvariant(currentTarget, "SomeClass", ContractClass.class, DummyClass.class);
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
