package de.andrena.c4j.acceptancetest.stack;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.systemtest.TransformerAwareRule;

public class StackTest {
	
	private Stack<Integer> classUnderTest;
	private final int CAPACITY = 5;

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void setUpTestCase() {
		// Auto-generated method stub
	}

	@AfterClass
	public static void tearDownTestCase() {
		// Auto-generated method stub
	}

	@Before
	public void setUpTest() {
		classUnderTest = new Stack<Integer>(CAPACITY);
	}

	@After
	public void tearDownTest() {
		classUnderTest = null;
	}

	@Test
	public void StackTestErrorCondition1() {
		// Test error condition for "capacity > 0"
		// assert capacity > 0 : "capacity > 0";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("capacity > 0");
		new Stack<Integer>(0);
	}

	@Test
	public void StackTestRight() {
		// Test condition for "capacity set"
		// assert capacity() == capacity : "capacity set";
		new Stack<Integer>(CAPACITY * 2);
	}

	@Test
	public void capacityTestRight() {
		// Test condition for "returnValue > 0"
		// assert returnValue > 0 : "returnValue > 0";
		classUnderTest.capacity();
	}

	@Test
	public void countTestRight() {
		// Test condition for "returnValue >= 0"
		// assert returnValue >= 0 : "returnValue >= 0";
		classUnderTest.count();
	}

	@Test
	public void countTestRight1() {
		// Test condition for "count <= capacity"
		// assert returnValue <= capacity() : "count <= capacity";
		classUnderTest.push(10);
		classUnderTest.count();
	}

	@Test
	public void pushTestErrorCondition1() {
		// Test error condition for "x != null"
		// assert x != null : "x != null";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("x != null");
		classUnderTest.push(null);
	}

	@Test
	public void pushTestErrorCondition2() {
		// Test error condition for "not isFull"
		// assert !isFull() : "not isFull";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("not isFull");
		int i = 0;
		while (!classUnderTest.isFull()) {
			classUnderTest.push(i);
			i = i + 1;
		}
		classUnderTest.push(i);
	}

	@Test
	public void pushTestRight() {
		// Test condition for "old count increased by 1"
		// assert count() == old count + 1 :
		// "old count increased by 1";
		classUnderTest.push(1);
	}

	@Test
	public void pushTestRight1() {
		// Test condition for "x set"
		// assert top() == x : "x set";
		classUnderTest.push(1);
	}

	@Test
	public void popTestErrorCondition1() {
		// Test error condition for "not isEmpty"
		// assert !isEmpty() : "not isEmpty";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("not isEmpty");
		classUnderTest.pop();
	}

	@Test
	public void popTestRight() {
		// Test condition for "old count decreased by 1"
		// assert count() == old count - 1 :
		// "old count decreased by 1";
		classUnderTest.push(1);
		classUnderTest.pop();
	}

	@Test
	public void popTestRight1() {
		// Test condition for "values unchanged"
		// assert old_values[i] == get(i) : "values unchanged";
		classUnderTest.push(1);
		classUnderTest.push(2);
		classUnderTest.push(3);
		classUnderTest.push(4);
		classUnderTest.pop();
	}

	@Test
	public void topTestErrorCondition1() {
		// Test error condition for "not isEmpty"
		// assert !isEmpty() : "not isEmpty";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("not isEmpty");
		classUnderTest.top();
	}

	@Test
	public void topTestRight() {
		// Test condition for "returnValue == top item"
		// assert returnValue == get(count() - 1) :
		// "returnValue == top item";
		classUnderTest.push(1);
		classUnderTest.top();
	}

	@Test
	public void isFullTestRight() {
		// Test condition for "count == capacity"
		// assert count() == capacity() : "count == capacity";
		int i = 0;
		while (!classUnderTest.isFull()) {
			classUnderTest.push(i);
			i = i + 1;
		}
		classUnderTest.isFull();
	}

	@Test
	public void isEmptyTestRight() {
		// Test condition for "count == 0"
		// assert count() == 0 : "count == 0";
		classUnderTest.push(1);
		classUnderTest.pop();
		classUnderTest.isEmpty();
	}

	@Test
	public void getTestErrorCondition1() {
		// Test error condition for "index >= 0"
		// assert index >= 0 : "index >= 0";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("index >= 0");
		classUnderTest.get(-1);
	}

	@Test
	public void getTestErrorCondition2() {
		// Test error condition for "index < count"
		// assert index < count() : "index < count";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("index < count");
		classUnderTest.push(1);
		classUnderTest.push(2);
		classUnderTest.get(classUnderTest.count());
	}

	@Test
	public void getTestRight() {
		classUnderTest.push(1);
		classUnderTest.push(2);
		classUnderTest.get(classUnderTest.count() - 1);
	}

}
