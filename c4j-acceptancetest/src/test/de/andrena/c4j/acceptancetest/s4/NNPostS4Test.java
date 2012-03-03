package de.andrena.c4j.acceptancetest.s4;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.acceptancetest.stack.Stack;
import de.andrena.c4j.acceptancetest.stack.StackContract;
import de.andrena.c4j.acceptancetest.stack.StackSpec;
import de.andrena.c4j.systemtest.TransformerAwareRule;

/**
 * Tests for postconditions of {@link StackContract}
 */
public class NNPostS4Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Stack<String> stack;

	@Before
	public void before() {
		stack = new Stack<String>(2);
	}

	@Test
	public void countPostconditionFulfilled() throws Exception {
		stack.count();
	}

	@Test
	public void countPostconditionViolatedLowerBound() throws Exception {
		Stack<String> brokenStack = new Stack<String>(10) {
			@Override
			public int count() {
				return -1;
			}
		};
		thrown.expect(AssertionError.class);
		thrown.expectMessage("result >= 0");
		brokenStack.count();
	}

	@Test
	public void countPostconditionViolatedUpperBound() throws Exception {
		Stack<String> brokenStack = new Stack<String>(10) {
			@Override
			public int count() {
				return 11;
			}
		};
		thrown.expect(AssertionError.class);
		thrown.expectMessage("count <= capacity");
		brokenStack.count();
	}

	@Test
	public void pushPostconditionFulfilled() throws Exception {
		stack.push("bottom");
	}

	@Test
	public void pushConditionViolatedByNotPushing() throws Exception {
		Stack<String> brokenStack = new Stack<String>(10) {
			@Override
			public String top() {
				return null;
			}
		};
		thrown.expect(AssertionError.class);
		thrown.expectMessage("x set");
		brokenStack.push("bottom");
	}

	@Test
	public void popPostconditionFulfilled() throws Exception {
		stack.push("string");
		stack.pop();
	}

	@Test
	public void popPostconditionViolatedByChangingValues() throws Exception {
		Stack<String> brokenStack = new Stack<String>(10) {
			@Override
			public void pop() {
				super.pop();
				try {
					List<String> values = getPrivateValuesFieldFromStack(this);
					values.set(0, "BROKEN");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		};
		brokenStack.push("string 1");
		brokenStack.push("string 2");
		thrown.expect(AssertionError.class);
		thrown.expectMessage("values unchanged");
		brokenStack.pop();
	}

	@Test
	public void topPostconditionFulfilled() throws Exception {
		String x = "teststring";
		stack.push(x);
		assertThat(stack.top(), is(sameInstance(x)));
	}

	@Test
	public void topPostconditionViolatedByReturningFifoInsteadOfLifo() throws Exception {
		Stack<String> brokenStack = new Stack<String>(2) {
			@Override
			public String top() {
				return get(0);
			}
		};
		List<String> values = getPrivateValuesFieldFromStack(brokenStack);
		values.add("first");
		values.add("second");
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("result == top_item"));
		brokenStack.top();
	}

	@Test
	public void isFullPostconditionFulfilled() throws Exception {
		stack.isFull();
		stack.push("first");
		stack.isFull();
		stack.push("last");
		stack.isFull();
	}

	@Test
	public void isFullPostcondtionViolatedInCaseOfFullStack() throws Exception {
		Stack<String> brokenStack = new Stack<String>(2) {
			@Override
			public boolean isFull() {
				return count() == 10;
			};
		};
		brokenStack.isFull();
		brokenStack.push("first");
		brokenStack.push("second");
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("count < capacity"));
		brokenStack.isFull();
	}

	@Test
	public void isFullPostcondtionViolatedInCaseOfNotFullStack() throws Exception {
		Stack<String> brokenStack = new Stack<String>(2) {
			@Override
			public boolean isFull() {
				return true;
			};
		};
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("count == capacity"));
		brokenStack.isFull();
	}

	@Test
	public void isEmptyPostconditionFulfilled() throws Exception {
		stack.isEmpty();
		stack.push("first");
		stack.isEmpty();
	}

	@Test
	public void isEmptyPostcondtionViolatedInCaseOfNotEmptyStack() throws Exception {
		Stack<String> brokenStack = new Stack<String>(2) {
			@Override
			public boolean isEmpty() {
				return true;
			};
		};
		brokenStack.isEmpty();
		brokenStack.push("first");
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("count == 0"));
		brokenStack.isEmpty();
	}

	@Test
	public void isEmptyPostcondtionViolatedInCaseOfEmptyStack() throws Exception {
		Stack<String> brokenStack = new Stack<String>(2) {
			@Override
			public boolean isEmpty() {
				return false;
			};
		};
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("count > 0"));
		brokenStack.isEmpty();
	}

	@Test
	public void capacityPostconditionFulfilled() throws Exception {
		stack.capacity();
	}

	@Test
	public void capacityPostconditionViolatedWithAnonymousSubclass() throws Exception {
		Stack<String> brokenStack = new Stack<String>(1) {
			// this condition is necessary because capacity() is already called during the verification of
			// the postcondition of the constructor of Stack<T>
			// and we want the constructor call to pass the contract
			@Override
			public int capacity() {
				if (new Exception().getStackTrace()[1].getClassName().equals(NNPostS4Test.class.getName())) {
					return -1;
				}
				return super.capacity();
			}
		};
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("result > 0"));
		brokenStack.capacity();
	}

	@Test
	public void capacityPostconditionViolatedWithBrokenStack() throws Exception {
		stack = new BrokenStack<String>(1);
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("result > 0"));
		stack.capacity();
	}

	@Test
	public void capacityPostconditionViolatedWithBrokenStackWithoutClassContract() throws Exception {
		BrokenStackWithoutClassContract<String> brokenStack = new BrokenStackWithoutClassContract<String>(1);
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("result > 0"));
		brokenStack.capacity();
	}

	private static class BrokenStack<T> extends Stack<T> {
		public BrokenStack(int capacity) {
			super(capacity);
		}

		@Override
		public int capacity() {
			if (new Exception().getStackTrace()[1].getClassName().equals(NNPostS4Test.class.getName())) {
				return -1;
			}
			return super.capacity();
		}
	}

	private static class BrokenStackWithoutClassContract<T> implements StackSpec<T> {
		private Stack<T> delegatee;

		public BrokenStackWithoutClassContract(int capacity) {
			delegatee = new Stack<T>(capacity);
		}

		@Override
		public int capacity() {
			return -1;
		}

		// methods delegated to delegatee

		@Override
		public int count() {
			return delegatee.count();
		}

		@Override
		public void push(T x) {
			delegatee.push(x);
		}

		@Override
		public void pop() {
			delegatee.pop();
		}

		@Override
		public T top() {
			return delegatee.top();
		}

		@Override
		public boolean isFull() {
			return delegatee.isFull();
		}

		@Override
		public boolean isEmpty() {
			return delegatee.isEmpty();
		}

		@Override
		public T get(int index) {
			return delegatee.get(index);
		}
	}

	private <T> List<T> getPrivateValuesFieldFromStack(Stack<T> stack) {
		Field valuesField;
		try {
			valuesField = Stack.class.getDeclaredField("values");
			valuesField.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<T> values = (List<T>) valuesField.get(stack);
			return values;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
