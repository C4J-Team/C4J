package de.vksi.c4j.acceptancetest.point.pitfalls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.acceptancetest.point.pitfalls.PointPFEclipseGen;

public class PointPFEclipseGenTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private PointPFEclipseGen p1;
	private PointPFEclipseGen p2;
	private PointPFEclipseGen p3;
	private HashSet<PointPFEclipseGen> hashSet;

	@Before
	public void setUp() {
		p1 = new PointPFEclipseGen(1, 2);
		p2 = new PointPFEclipseGen(1, 2);
		p3 = new PointPFEclipseGen(1, 1);
		hashSet = new HashSet<PointPFEclipseGen>();
		hashSet.add(p1);
	}

	@Test
	public void testEquals() {
		assertTrue(p1.equals(p2));
		assertFalse(p1.equals(p3));
	}

	@Test
	public void testEquals_SearchInHashSet() {
		assertTrue(hashSet.contains(p1));
		assertTrue(hashSet.contains(p2));
		assertFalse(hashSet.contains(p3));
	}

	@Test
	public void testEquals_PointShouldBeFoundInHashSet() {
		thrown.expect(AssertionError.class);
		p1.setX(2);
		p1.setY(3);
		assertTrue(hashSet.contains(p1));
	}

}