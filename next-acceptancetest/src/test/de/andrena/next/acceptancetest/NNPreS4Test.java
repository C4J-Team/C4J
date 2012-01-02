package de.andrena.next.acceptancetest;

import static de.andrena.next.Condition.ignored;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.Condition.PreCondition;
import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

/**
 * Testing preconditions on methods in classes
 */
public class NNPreS4Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private MiniCollection<String> dummy;

	@Before
	public void before() {
		dummy = new MiniCollection<String>();
	}

	@Test
	public void preconditionFulfilled() {
		dummy.get(3);
	}

	@Test
	public void preconditionViolated() {
		thrown.expect(AssertionError.class);
		dummy.get(0);
	}

	@Test
	public void preconditionWithTwoParametersFulfilled() {
		dummy.insertAt(3, "Teststring");
	}

	@Test
	public void preconditionWithTwoParametersSecondParameterViolated() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("must not be null"));
		dummy.insertAt(3, null);
	}

	@Test
	public void preconditionWithVarArgsFulfilled() {
		dummy.append("String 1", "String 2");
	}

	@Test
	public void preconditionViolatedByNullElementInVarArg() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("must not contain null elements"));
		dummy.append("String 1", null, "String 3");
	}

	@Contract(MiniCollectionContract.class)
	public static class MiniCollection<T> {
		public T get(int index) {
			return null;
		}

		public void insertAt(int index, T value) {
		}

		public void append(T... values) {
		}
	}

	public static class MiniCollectionContract<T> extends MiniCollection<T> {
		@Override
		public T get(final int index) {
			new PreCondition() {
				{
					assert index > 0 : "index greater 0";
				}
			};
			return ignored();
		}

		public void insertAt(final int index, final T value) {
			new PreCondition() {
				{
					assert index > 0 : "index greater 0";
					assert value != null : "value must not be null";
				}
			};
		};

		public void append(final T... values) {
			new PreCondition() {
				{
					assert values != null : "values must not be null";
					assert values.length > 0 : "values must at least contain one element";
					for (T value : values) {
						assert value != null : "values must not contain null elements";
					}
				}
			};
		};
	}

}
