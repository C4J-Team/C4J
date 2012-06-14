package de.vksi.c4j.acceptancetest.floatingwindow;

import static de.vksi.c4j.Condition.preCondition;

public class NorthEastAndSouthWestFloatingWindowContract extends NorthEastAndSouthWestFloatingWindow {

	public NorthEastAndSouthWestFloatingWindowContract(Vector upperLeftCorner, int width, int height) {
		super(upperLeftCorner, width, height);	
	}
	
	@SuppressWarnings("unused")
	@Override
	public void move(Vector vector) {
		if(preCondition()) {
			assert true : "true";
		}
	}

}
