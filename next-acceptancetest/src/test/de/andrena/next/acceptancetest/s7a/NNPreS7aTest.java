package de.andrena.next.acceptancetest.s7a;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.pre;

import de.andrena.next.Contract;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNPreS7aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private NorthEastFloatingWindowSpec northEastFloatingWindow;
	
	@Test
	public void testErrorByMultipleInheritance() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("vector.x > 0");
		northEastFloatingWindow = new NorthEastAndSouthWestFloatingWindow(new Vector(0,0), 200, 200);
		//Move window to SouthWest 
		northEastFloatingWindow.move(new Vector(-2, -5));
	}
	
	private interface Window {
		
		Vector getUpperLeftCorner();
		
		int getWidth();
		
		int getHeight();
		
	}
	
	@Contract(NorthEastFloatingWindowContract.class)
	public interface NorthEastFloatingWindowSpec extends Window {
		
		void move(Vector vector);
		
	}
	
	public static class NorthEastFloatingWindowContract implements NorthEastFloatingWindowSpec {

		@Override
		public Vector getUpperLeftCorner() {
			// No contracts identified yet
			return ignored();
		}

		@Override
		public int getWidth() {
			// No contracts identified yet
			return ignored();
		}

		@Override
		public int getHeight() {
			// No contracts identified yet
			return ignored();
		}

		@Override
		public void move(Vector vector) {
			if(pre()) {
				assert vector.x > 0 : "vector.x > 0";
				assert vector.y > 0 : "vector.y > 0";
			}		
		}
		
	}
	
	@Contract(SouthWestFloatingWindowContract.class)
	public interface SouthWestFloatingWindowSpec extends Window {
		
		void move(Vector vector);
		
	}
	
	public static class SouthWestFloatingWindowContract implements SouthWestFloatingWindowSpec {

		@Override
		public Vector getUpperLeftCorner() {
			// No contracts identified yet
			return ignored();
		}

		@Override
		public int getWidth() {
			// No contracts identified yet
			return ignored();
		}

		@Override
		public int getHeight() {
			// No contracts identified yet
			return ignored();
		}

		@Override
		public void move(Vector vector) {
			if(pre()) {
				assert vector.x < 0 : "vector.x < 0";
				assert vector.y < 0 : "vector.y < 0";
			}		
		}
		
	}
	
	public class NorthEastAndSouthWestFloatingWindow implements NorthEastFloatingWindowSpec, SouthWestFloatingWindowSpec {

		private Vector upperLeftCorner;		
		private int width;		
		private int height;
		
		public NorthEastAndSouthWestFloatingWindow(Vector upperLeftCorner, int width, int height) {
			this.upperLeftCorner = upperLeftCorner;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Vector getUpperLeftCorner() {
			return upperLeftCorner;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public void move(Vector vector) {
			upperLeftCorner.add(vector);
		}
		
	}
	
	public class Vector {
		
		int x, y;
		
		public Vector(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void add(Vector vector) {
			this.x += vector.x;
			this.y += vector.y;
		}
		
	}
	
}
