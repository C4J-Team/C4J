package de.vksi.c4j.acceptancetest.floatingwindow;

import de.vksi.c4j.ContractReference;

@ContractReference(SouthWestFloatingWindowSpecContract.class)
public interface SouthWestFloatingWindowSpec extends Window {

	void move(Vector vector);
	
}
