package de.andrena.next.systemtest.pure;

import org.junit.Test;

import de.andrena.next.Pure;

public class PurePassingParametersToUnpureMethodsSystemTest {

	@Test(expected = AssertionError.class)
	public void testPurePassingParametersToUnpureMethods() {
		TargetClass target = new TargetClass();
		target.move(3);
	}

	@Test(expected = AssertionError.class)
	public void testStaticPurePassingParametersToUnpureMethods() {
		TargetClass.moveStatic(3);
	}

	public static class TargetClass {
		private Position position = new Position();
		private static Position positionStatic = new Position();

		public int getPosition() {
			return position.getValue();
		}

		@Pure
		public void move(int value) {
			PositionChanger changer = new PositionChanger(position);
			changer.updatePosition(value);
		}

		@Pure
		public static void moveStatic(int value) {
			PositionChanger changer = new PositionChanger(positionStatic);
			changer.updatePosition(value);
		}
	}

	public static class Position {
		private int value;

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class PositionChanger {
		private Position position;

		public PositionChanger(Position position) {
			this.position = position;
		}

		public void updatePosition(int value) {
			position.setValue(value);
		}
	}
}
