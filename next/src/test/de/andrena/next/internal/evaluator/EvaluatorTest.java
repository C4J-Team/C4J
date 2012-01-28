package de.andrena.next.internal.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.andrena.next.ClassInvariant;
import de.andrena.next.internal.evaluator.Evaluator.EvaluationPhase;

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
		Evaluator.storeFieldAccess("dummyField");
		assertEquals("someValue", Evaluator.oldFieldAccess("dummyField"));
	}

	@Test
	public void testOldMethodCall() {
		Evaluator.currentTarget.set(currentTarget);
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
	public void testBefore() {
		Evaluator.before(currentTarget, ContractClassForBefore.class, DummyClass.class, "contractMethod",
				new Class<?>[0], new Object[0]);
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	public static class ContractClassForBefore {
		public void contractMethod() {
			assertEquals(EvaluationPhase.BEFORE, Evaluator.evaluationPhase.get());
			assertEquals(currentTarget, Evaluator.currentTarget.get());
		}
	}

	@Test
	public void testAfter() {
		Evaluator.after(currentTarget, ContractClassForAfter.class, DummyClass.class, "contractMethod",
				new Class<?>[0], new Object[0], Integer.valueOf(4));
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	public static class ContractClassForAfter {
		public int contractMethod() {
			assertEquals(EvaluationPhase.AFTER, Evaluator.evaluationPhase.get());
			assertEquals(currentTarget, Evaluator.currentTarget.get());
			assertEquals(Integer.valueOf(4), Evaluator.returnValue.get());
			return 0;
		}
	}

	@Test
	public void testCallInvariant() {
		Evaluator.callInvariant(currentTarget, ContractClassForInvariant.class, DummyClass.class, "invariant");
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	public static class ContractClassForInvariant {
		@ClassInvariant
		public void invariant() {
			assertEquals(EvaluationPhase.INVARIANT, Evaluator.evaluationPhase.get());
			assertEquals(currentTarget, Evaluator.currentTarget.get());
		}
	}

	@Test
	public void testCallContractMethod() {
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		Evaluator.callContractMethod(ContractClass.class, DummyClass.class, "contractMethod", new Class<?>[0],
				new Object[0]);
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	@Test
	public void testCallContractMethodThrowingException() {
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		try {
			Evaluator.callContractMethod(ContractClass.class, DummyClass.class, "contractMethodThrowingException",
					new Class<?>[0], new Object[0]);
			fail("expected EvaluationException");
		} catch (EvaluationException e) {
			// expected
		}
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	@Test
	public void testCallContractMethodThrowingAssertionError() {
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		try {
			Evaluator.callContractMethod(ContractClass.class, DummyClass.class, "contractMethodThrowingAssertionError",
					new Class<?>[0], new Object[0]);
			fail("expected AssertionError");
		} catch (AssertionError e) {
			// expected
		}
		assertEquals(EvaluationPhase.NONE, Evaluator.evaluationPhase.get());
	}

	public static class ContractClass {
		public int contractMethod() {
			assertEquals(int.class, Evaluator.contractReturnType.get());
			return 0;
		}

		public int contractMethodThrowingException() {
			throw new RuntimeException();
		}

		public int contractMethodThrowingAssertionError() {
			assert false;
			return 0;
		}
	}

	@Test
	public void testCallContractMethodRetainingState() {
		Evaluator.evaluationPhase.set(EvaluationPhase.AFTER);
		Evaluator.currentTarget.set(currentTarget);
		Evaluator.callContractMethod(ContractClassRetainingState.class, DummyClass.class, "contractMethod",
				new Class<?>[0], new Object[0]);
		Evaluator.callContractMethod(ContractClassRetainingState.class, DummyClass.class, "contractMethod",
				new Class<?>[0], new Object[0]);
	}

	public static class ContractClassRetainingState {
		private static ContractClassRetainingState instance;

		public void contractMethod() {
			if (instance != null) {
				assertTrue(this == instance);
			}
			instance = this;
		}
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
