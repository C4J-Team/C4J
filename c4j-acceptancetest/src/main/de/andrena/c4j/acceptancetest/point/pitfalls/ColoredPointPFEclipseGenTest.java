package de.andrena.c4j.acceptancetest.point.pitfalls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.acceptancetest.point.Color;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ColoredPointPFEclipseGenTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ColoredPointPFEclipseGen p1;
	private ColoredPointPFEclipseGen p2;
	private ColoredPointPFEclipseGen p3;
	private PointPFEclipseGen p4;
	private HashSet<ColoredPointPFEclipseGen> hashSet;

	@Before
	public void setUp() {
		p1 = new ColoredPointPFEclipseGen(1, 2, Color.BLUE);
		p2 = new ColoredPointPFEclipseGen(1, 2, Color.BLUE);
		p3 = new ColoredPointPFEclipseGen(1, 2, Color.GREEN);
		p4 = new PointPFEclipseGen(1, 2);
		hashSet = new HashSet<ColoredPointPFEclipseGen>();
		hashSet.add(p1);
	}

	@Test
	public void testEquals() {
		assertTrue(p1.equals(p2));
		assertFalse(p1.equals(p3));
		assertFalse(p1.equals(p4));
	}

	@Test
	public void testEquals_SearchInHashSet() {
		assertTrue(hashSet.contains(p1));
		assertTrue(hashSet.contains(p2));
		assertFalse(hashSet.contains(p3));
		assertFalse(hashSet.contains(p4));
	}

	@Test
	public void testEquals_PointShouldBeFoundInHashSet() {
		thrown.expect(AssertionError.class);
		p1.setX(2);
		p1.setY(3);
		assertTrue(hashSet.contains(p1));
	}

}