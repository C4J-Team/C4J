package de.andrena.c4j.acceptancetest.point.pitfalls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.systemtest.TransformerAwareRule;

public class PointPF2Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private PointPF2 p1;
	private PointPF2 p2;
	private PointPF2 p3;
	private HashSet<PointPF2> hashSet;

	@Before
	public void setUp() {
		p1 = new PointPF2(1, 2);
		p2 = new PointPF2(1, 2);
		p3 = new PointPF2(1, 1);
		hashSet = new HashSet<PointPF2>();
		hashSet.add(p1);
	}

	@Test
	public void testEquals() {
		assertFalse(p1.equals(p3));
	}

	@Test
	public void testEquals_HashCodeConsistencyError() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("is consistent with hashCode");
		assertTrue(p1.equals(p2));
	}

	@Test
	public void testEquals_SearchInHashSet() {
		assertTrue(hashSet.contains(p1));
		assertFalse(hashSet.contains(p3));
	}

	@Test
	public void testEquals_PointShouldBeFoundInHashSet() {
		thrown.expect(AssertionError.class);
		assertTrue(hashSet.contains(p2));
	}

}
