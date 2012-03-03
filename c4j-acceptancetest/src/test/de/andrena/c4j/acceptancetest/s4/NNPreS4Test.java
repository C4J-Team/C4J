package de.andrena.c4j.acceptancetest.s4;

import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.acceptancetest.stack.Stack;
import de.andrena.c4j.acceptancetest.stack.StackContract;
import de.andrena.c4j.systemtest.TransformerAwareRule;

/**
 * Testing preconditions of {@link StackContract}
 */
public class NNPreS4Test {

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
	public void constructorPreconditionFulfilled() throws Exception {
		new Stack<String>(4711);
	}
	
	@Test
	public void constructorPreconditionViolatedForZeroCapacity() throws Exception {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("> 0"));
		new Stack<String>(0);
	}

	@Test
	public void getPreconditionFulfilled() {
		stack.push("bottom");
		stack.get(0);
	}
	
	@Test
	public void getPreconditionViolatedOnNegativeIndices() {
		thrown.expect(AssertionError.class);
		stack.get(-1);
	}

	@Test
	public void getPreconditionViolatedOnEmptyStacks() {
		thrown.expect(AssertionError.class);
		stack.get(0);
	}

	@Test
	public void getPreconditionViolatedOnGetOutOfCount() {
		stack.push("bottom");
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("< count"));
		stack.get(1);
	}
	
	@Test
	public void pushPreconditionFulfilled() {
		stack.push("bottom");
		stack.push("second");
	}

	@Test
	public void pushPreconditionVilolatedForNullValues() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("!= null"));
		stack.push(null);
	}

	@Test
	public void pushPreconditionVilolatedForFullStacks() {
		stack.push("bottom");
		stack.push("second");
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("not isFull"));
		stack.push("third");
	}
	
	@Test
	public void topPreconditionFulfilled() throws Exception {
		stack.push("bottom");
		stack.top();
	}
	
	@Test
	public void topPreconditionViolatedOnEmptyStacks() throws Exception {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("not isEmpty"));
		stack.top();
	}
	
	@Test
	public void popPreconditionFulfilled() throws Exception {
		stack.push("bottom");
		stack.pop();
	}
	
	@Test
	public void popPreconditionViolatedOnEmptyStack() throws Exception {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("not isEmpty"));
		stack.pop();
	}

}
