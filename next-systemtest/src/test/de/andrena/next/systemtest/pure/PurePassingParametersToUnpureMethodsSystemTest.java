package de.andrena.next.systemtest.pure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.andrena.next.Pure;

public class PurePassingParametersToUnpureMethodsSystemTest {

	@Test(expected = AssertionError.class)
	public void testPurePassingParametersToUnpureMethods() {
		TargetClass target = new TargetClass();
		target.move(3);
		assertEquals(3, target.getPosition());
	}

	public static class TargetClass {
		private Position position = new Position();

		public int getPosition() {
			return position.getValue();
		}

		@Pure
		public void move(int value) {
			// PureEvaluator.registerUnpure(position);
			PositionChanger changer = new PositionChanger(position);
			changer.updatePosition(value);
			// PureEvaluator.unregisterUnpure(position);
		}
	}

	public static class Position {
		private int value;

		public int getValue() {
			// PureEvaluator.checkUnpureAccess(this);
			return value;
		}

		public void setValue(int value) {
			// PureEvaluator.checkUnpureAccess(this);
			this.value = value;
		}
	}

	public static class PositionChanger {
		private Position position;

		public PositionChanger(Position position) {
			this.position = position;
		}

		public void updatePosition(int value) {
			// PureEvaluator.checkUnpureAccess(this);
			position.setValue(value);
		}
	}
}
