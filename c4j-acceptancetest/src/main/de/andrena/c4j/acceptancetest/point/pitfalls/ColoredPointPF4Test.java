package de.andrena.c4j.acceptancetest.point.pitfalls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.acceptancetest.point.Color;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ColoredPointPF4Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ColoredPointPF4 p1;
	private ColoredPointPF4 p2;
	private ColoredPointPF4 p3;
	private PointPF4 p4;

	@Before
	public void setUp() {
		p1 = new ColoredPointPF4(1, 2, Color.BLUE);
		p2 = new ColoredPointPF4(1, 2, Color.BLUE);
		p3 = new ColoredPointPF4(1, 2, Color.GREEN);
		p4 = new PointPF4(1, 2);
	}

	@Test
	public void testEquals_OK() {
		assertTrue(p1.equals(p2));
		assertFalse(p1.equals(p3));
	}

	@Test
	public void testEquals_SymmetryError() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("is symmetric");
		assertFalse(p1.equals(p4));
	}

}
