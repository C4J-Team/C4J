package de.andrena.c4j.acceptancetest.floatingwindow;

import de.andrena.c4j.ContractReference;

@ContractReference(SouthWestFloatingWindowSpecContract.class)
public interface SouthWestFloatingWindowSpec extends Window {

	void move(Vector vector);
	
}
