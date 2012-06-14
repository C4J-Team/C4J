package de.vksi.c4j.acceptancetest.point.pitfalls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.acceptancetest.point.pitfalls.PointPF1;

public class PointPF1Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private PointPF1 p1;
	private PointPF1 p2;
	private PointPF1 p3;
	private HashSet<PointPF1> hashSet;

	@Before
	public void setUp() {
		p1 = new PointPF1(1, 2);
		p2 = new PointPF1(1, 2);
		p3 = new PointPF1(1, 1);
		hashSet = new HashSet<PointPF1>();
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
		assertFalse(hashSet.contains(p3));
	}

	@Test
	public void testEquals_PointShouldBeFoundInHashSet() {
		thrown.expect(AssertionError.class);
		assertTrue(hashSet.contains(p2));
	}

}
