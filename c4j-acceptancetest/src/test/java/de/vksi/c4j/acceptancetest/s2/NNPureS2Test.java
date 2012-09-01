package de.vksi.c4j.acceptancetest.s2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.Pure;
import de.vksi.c4j.acceptancetest.floatingwindow.Vector;
import de.vksi.c4j.acceptancetest.point.Color;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class NNPureS2Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testIncorrectPureUsage_IllegalMethodAccessOnBoxedField() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(JUnitMatchers.containsString("illegal access on unpure object"));

		FloatingWindow window = new FloatingWindow(new Vector(0, 0));
		window.move(new Vector(5, 10));
	}

	@Test
	public void testIncorrectPureUsage_IllegalFieldWriteAccess() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(JUnitMatchers.containsString("illegal write access on field"));

		FloatingWindow window = new FloatingWindow(new Vector(0, 0));
		window.setStyleColor(Color.GREEN);
	}

	@Test
	public void testCorrectPureUsage_ChangesOnLocalVariablesAllowed() {
		FloatingWindow window = new FloatingWindow(new Vector(1, 3));
		assertEquals("UpperLeftCorner: 1, 3\nColor: INDIGO", window.toString());
	}

	@Test
	public void testCorrectPureUsage_ReferenceCopySetToNullDoesNotChangeObject() {
		FloatingWindow window = new FloatingWindow(new Vector(1, 3));
		window.destroyUpperLeftCorner();
		assertNotNull(window.upperLeftCorner);
	}

	@Test
	public void testIncorrectPureUsage_FieldModifiedInPureMethodOfAnotherType() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(JUnitMatchers.containsString("illegal access on unpure object"));
		FloatingWindow window = new FloatingWindow(new Vector(1, 3));
		window.resetUpperLeftCorner();
	}

	@Test
	public void testIncorrectPureUsage_IllegalFieldModification() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(JUnitMatchers.containsString("illegal write access on field"));
		FloatingWindow window = new FloatingWindow(new Vector(1, 3));
		window.reallyDestroyUpperLeftCorner();
	}

	private class FloatingWindow {
		public Vector upperLeftCorner;
		public WindowStyle style;
		private VectorDestroyer destroyer;

		public FloatingWindow(Vector upperLeftCorner) {
			this.upperLeftCorner = upperLeftCorner;
			style = new WindowStyle();
			destroyer = new VectorDestroyer();
		}

		@Pure
		//method does nothing
		public void destroyUpperLeftCorner() {
			destroyer.destroyVector(upperLeftCorner);
		}

		@Pure
		//method does nothing
		public void reallyDestroyUpperLeftCorner() {
			upperLeftCorner = null;
		}

		@Pure
		//Incorrect use of pure
		public void resetUpperLeftCorner() {
			destroyer.resetVector(upperLeftCorner);
		}

		@Pure
		//Incorrect use of pure
		public void move(Vector vector) {
			VectorBox upperLeftCornerBoxed = new VectorBox(upperLeftCorner);
			upperLeftCornerBoxed.add(vector);
		}

		@Pure
		//Incorrect use of pure
		public void setStyleColor(Color color) {
			style.color = color;
		}

		@Pure
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("UpperLeftCorner: " + upperLeftCorner.getX() + ", " + upperLeftCorner.getY() + "\n");
			buffer.append("Color: " + style.color);
			return buffer.toString();
		}
	}

	private class VectorDestroyer {
		@Pure
		public void destroyVector(Vector vector) {
			vector = null;
		}

		@Pure
		public void resetVector(Vector vector) {
			vector.setX(0);
			vector.setY(0);
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

	private class WindowStyle {
		public Color color = Color.INDIGO;
	}

}
