package de.andrena.next.acceptancetest.s4;

import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.acceptancetest.stack.Stack;
import de.andrena.next.acceptancetest.stack.StackContract;
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
	public void capacityPostConditionViolated() throws Exception {
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
		thrown.expectMessage(containsString("capacity > 0"));
		stack.capacity();
	}

}
