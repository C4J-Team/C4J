package de.andrena.next.acceptancetest.s4;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.acceptancetest.stack.Stack;
import de.andrena.next.acceptancetest.stack.StackContract;
import de.andrena.next.acceptancetest.stack.StackSpec;
import de.andrena.next.systemtest.TransformerAwareRule;

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
					Field valuesField = this.getClass().getSuperclass().getDeclaredField("values");
					valuesField.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<String> values = (List<String>) valuesField.get(this);
					values.set(0, "BROKEN");
				} catch (Exception e) {
					System.out.println(e);
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
	public void capacityPostconditionFulfilled() throws Exception {
		stack.capacity();
	}
	
	@Test
	public void capacityPostconditionViolatedWithAnonymousSubclass() throws Exception {
		Stack<String> brokenStack = new Stack<String>(1) {
			// this marker is necessary because capacity() is already called during the verification of
			// the postcondition of the constructor of Stack<T>
			// and we want the constructor call to pass the contract
			private boolean capacityCalled = false;
			@Override
			public int capacity() {
				if (!capacityCalled) {
					capacityCalled = true;
					return super.capacity(); 
				}
				return -1;
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
		private boolean capacityCalled = false;
		public BrokenStack(int capacity) {
			super(capacity);
		}
		@Override
		public int capacity() {
			if (!capacityCalled) {
				capacityCalled = true;
				return super.capacity(); 
			}
			return -1;
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
		
		public int count() {
			return delegatee.count();
		}
		public void push(T x) {
			delegatee.push(x);
		}
		public void pop() {
			delegatee.pop();
		}
		public T top() {
			return delegatee.top();
		}
		public boolean isFull() {
			return delegatee.isFull();
		}
		public boolean isEmpty() {
			return delegatee.isEmpty();
		}
		public T get(int index) {
			return delegatee.get(index);
		}
	}

}
