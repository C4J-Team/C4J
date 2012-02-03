package de.andrena.next.acceptancetest.s2;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import de.andrena.next.Pure;
import de.andrena.next.acceptancetest.floatingwindow.Vector;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNPureS2Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void throwAssertionErrorByIncorrectUseOfPure() {		
		thrown.expect(AssertionError.class);
		thrown.expectMessage(JUnitMatchers.containsString("illegal method access on unpure method"));
		
		FloatingWindow window = new FloatingWindow(new Vector(0, 0));
		window.move(new Vector(5,10));
		
		//assert that the state of the window has changed
		assertEquals(5, window.upperLeftCorner.getX());
		assertEquals(10, window.upperLeftCorner.getY());
	}
	
	private class FloatingWindow {
		public Vector upperLeftCorner;
		
		public FloatingWindow(Vector upperLeftCorner) {
			this.upperLeftCorner = upperLeftCorner;
		}
		
		//Incorrect use of pure
		@Pure
		public void move(Vector vector) {
			VectorBox upperLeftCornerBoxed = new VectorBox(upperLeftCorner);
			upperLeftCornerBoxed.add(vector);
		}
	}
	
	private class VectorBox {
		Vector vector;
		
		public VectorBox(Vector vector) {
			this.vector = vector;
		}
		
		public void add(Vector vectorToAdd) {
			vector.add(vectorToAdd);
		}
	}
	
}
