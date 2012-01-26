package de.andrena.next.acceptancetest.point;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.systemtest.TransformerAwareRule;

public class PointTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Point classUnderTest;

	@Before
	public void setUpTest() {
		classUnderTest = new Point(1, 2);
	}

	@After
	public void tearDownTest() {
		classUnderTest = null;
	}

	@Test
	public void PointTestRight() {
		// Test post-condition for "x set"
		// assert m_target.getX() == x : "x set";
		new Point(1, 0);
	}

	@Test
	public void PointTestRight1() {
		// Test post-condition for "y set"
		// assert m_target.getY() == y : "y set";
		new Point(0, 2);
	}

	@Test
	public void getXTestRight() {
		assertEquals(1, classUnderTest.getX());
	}

	@Test
	public void getYTestRight() {
		assertEquals(2, classUnderTest.getY());
	}

	@Test
	public void setXTestRight() {
		// Test post-condition for "x set"
		// assert m_target.getX() == x : "x set";
		classUnderTest.setX(3);
	}

	@Test
	public void setYTestRight() {
		// Test post-condition for "y set"
		// assert m_target.getY() == y : "y set";
		classUnderTest.setY(4);
	}

	@Test
	public void equalsTestRight() {
		// Test post-condition for "if obj == null then false"
		// assert returnValue == false : "if obj == null then false";
		Point x = null;
		assertFalse(classUnderTest.equals(x));
	}

	@Test
	public void equalsTestRight1() {
		// Test post-condition for "is reflexive"
		// assert x.equals(x) : "is reflexive";
		Point x = classUnderTest;
		assertTrue(x.equals(x));
	}

	@Test
	public void equalsTestRight2() {
		// Test post-condition for "is symmetric"
		// assert x.equals(y) == y.equals(x) : "is symmetric";
		Point x = classUnderTest;
		Point y = new Point(x.getX(), x.getY());
		assertTrue(x.equals(y));
	}

	@Test
	public void equalsTestRight3() {
		// Test post-condition for "is transitive"
		// assert x.equals(z) : "is transitive";
		Point x = classUnderTest;
		Point y = new Point(x.getX(), x.getY());
		Point z = new Point(x.getX(), x.getY());
		assertTrue(x.equals(y));
		assertTrue(x.equals(z));
		assertTrue(y.equals(z));
	}

	@Test
	public void equalsTestRight4() {
		// Test post-condition for "is consistent with equals"
		// assert x.equals(y) == x.equals(y) : "is consistent with equals";
		Point x = classUnderTest;
		Point y = new Point(x.getX(), x.getY());
		assertTrue(x.equals(y));
		assertTrue(x.equals(y));
	}

	@Test
	public void equalsTestRight5() {
		// Test post-condition for "is consistent with hashCode"
		// assert x.hashCode() == y.hashCode() : "is consistent with hashCode";
		Point x = classUnderTest;
		Point y = new Point(x.getX(), x.getY());
		assertTrue(x.equals(y));
	}

	@Test
	public void hashCodeTestRight() {
		// Test post-condition for "is immutable"
		// assert returnValue == hashCode : "is immutable";
		int hashCode = classUnderTest.hashCode();
		classUnderTest.setX(5);
		classUnderTest.setY(6);
		assertEquals(hashCode, classUnderTest.hashCode());
	}

	@Test
	public void toStringTestRight() {
		// Test post-condition for "result not null"
		// assert returnValue != null : "result not null";
		assertNotNull(classUnderTest.toString());
	}

}