package de.vksi.c4j.acceptancetest.point;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.acceptancetest.point.Color;
import de.vksi.c4j.acceptancetest.point.ColoredPoint;
import de.vksi.c4j.acceptancetest.point.Point;

public class ColoredPointTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ColoredPoint classUnderTest;

	@Before
	public void setUpTest() {
		classUnderTest = new ColoredPoint(1, 2, Color.BLUE);
	}

	@After
	public void tearDownTest() {
		classUnderTest = null;
	}

	@Test
	public void ColoredPointTestErrorCondition1() {
		// Test error condition for "color not null"
		// assert color != null : "color not null";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("color not null");
		new ColoredPoint(1, 2, (Color) null);
	}

	@Test
	public void ColoredPointTestRight() {
		// Test post-condition for "x set"
		// assert m_target.getX() == x : "x set";
		new ColoredPoint(1, 0, Color.BLUE);
	}

	@Test
	public void ColoredPointTestRight1() {
		// Test post-condition for "y set"
		// assert m_target.getY() == y : "y set";
		new ColoredPoint(0, 2, Color.BLUE);
	}

	@Test
	public void ColoredPointTestRight2() {
		// Test post-condition for "color set"
		// assert m_target.getColor() == color : "color set";
		new ColoredPoint(0, 0, Color.GREEN);
	}

	@Test
	public void getColorTestRight() {
		// Test post-condition for "result not null"
		// assert returnValue != null : "result not null";
		classUnderTest = new ColoredPoint(1, 0, Color.GREEN);
		assertTrue(classUnderTest.getColor().equals(Color.GREEN));
	}

	@Test
	public void setColorTestErrorCondition1() {
		// Test error condition for "color not null"
		// assert color != null : "color not null";
		thrown.expect(AssertionError.class);
		thrown.expectMessage("color not null");
		classUnderTest.setColor((Color) null);
	}

	@Test
	public void setColorTestRight() {
		// Test post-condition for "color set"
		// assert m_target.getColor() == color : "color set";
		classUnderTest = new ColoredPoint(1, 0, Color.BLUE);
		classUnderTest.setColor(Color.GREEN);
	}

	@Test
	public void equalsTestRight() {
		// Test post-condition for "if obj == null then false"
		// assert returnValue == false : "if obj == null then false";
		assertFalse(classUnderTest.equals((Object) null));
	}

	@Test
	public void equalsTestRight1() {
		// Test post-condition for "is reflexive"
		// assert x.equals(x) : "is reflexive";
		ColoredPoint x = classUnderTest;
		assertTrue(x.equals(x));
	}

	@Test
	public void equalsTestRight2() {
		// Test post-condition for "is symmetric"
		// assert x.equals(y) == y.equals(x) : "is symmetric";
		ColoredPoint x = classUnderTest;
		Point y = new Point(x.getX(), x.getY());
		assertFalse(x.equals(y));
	}

	@Test
	public void equalsTestRight3() {
		// Test post-condition for "is transitive"
		// assert x.equals(z) : "is transitive";
		ColoredPoint x = classUnderTest;
		ColoredPoint y = new ColoredPoint(x.getX(), x.getY(), x.getColor());
		ColoredPoint z = new ColoredPoint(x.getX(), x.getY(), x.getColor());
		assertTrue(x.equals(y));
		assertTrue(y.equals(z));
		assertTrue(x.equals(z));
	}

	@Test
	public void equalsTestRight4() {
		// Test post-condition for "is consistent with equals"
		// assert x.equals(y) == x.equals(y) : "is consistent with equals";
		ColoredPoint x = classUnderTest;
		ColoredPoint y = new ColoredPoint(x.getX(), x.getY(), x.getColor());
		assertTrue(x.equals(y));
		assertTrue(x.equals(y));
	}

	@Test
	public void equalsTestRight5() {
		// Test post-condition for "is consistent with hashCode"
		// assert x.hashCode() == y.hashCode() : "is consistent with hashCode";
		ColoredPoint x = classUnderTest;
		ColoredPoint y = new ColoredPoint(x.getX(), x.getY(), x.getColor());
		assertTrue(x.equals(y));
	}

	@Test
	public void toStringTestRight() {
		// Test post-condition for "result not null"
		// assert returnValue != null : "result not null";
		assertNotNull(classUnderTest.toString());
	}

	@Test
	public void testEqualsAfterFirstHashCode() {
		ColoredPoint point1 = new ColoredPoint(3, 4, Color.BLUE);
		ColoredPoint point2 = new ColoredPoint(4, 4, Color.BLUE);
		point2.setX(3);
		assertTrue(point1.equals(point2));
	}

}