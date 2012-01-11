package de.andrena.next.acceptancetest.s4;

import static org.junit.matchers.JUnitMatchers.containsString;

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
	public void capacityPostConditionFulfilled() throws Exception {
		stack.capacity();
	}
	
	@Test
	public void capacityPostConditionViolatedWithAnonymousSubclass() throws Exception {
		stack = new Stack<String>(1) {
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
		stack.capacity();
	}
	
	@Test
	public void capacityPostConditionViolatedWithBrokenStack() throws Exception {
		stack = new BrokenStack<String>(1);
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("result > 0"));
		stack.capacity();
	}
	
	@Test
	public void capacityPostConditionViolatedWithBrokenStackWithoutStackContract() throws Exception {
		BrokenStackWithoutStackContract<String> brokenStack = new BrokenStackWithoutStackContract<String>(1);
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
	
	private static class BrokenStackWithoutStackContract<T> implements StackSpec<T> {
		private Stack<T> delegatee;
		public BrokenStackWithoutStackContract(int capacity) {
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
