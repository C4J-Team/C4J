package de.andrena.next.acceptancetest.point.pitfalls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.systemtest.TransformerAwareRule;

public class PointPF3Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private PointPF3 p1;
	private PointPF3 p2;
	private PointPF3 p3;
	private HashSet<PointPF3> hashSet;

	@Before
	public void setUp() {
		p1 = new PointPF3(1, 2);
		p2 = new PointPF3(1, 2);
		p3 = new PointPF3(1, 1);
		hashSet = new HashSet<PointPF3>();
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
		thrown.expectMessage("is immutable");
		p1.setX(2);
		p1.setY(3);
		assertTrue(hashSet.contains(p1));
	}

}
