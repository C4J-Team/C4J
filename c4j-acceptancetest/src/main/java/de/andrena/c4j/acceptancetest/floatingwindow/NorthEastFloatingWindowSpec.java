package de.andrena.c4j.acceptancetest.floatingwindow;

import de.andrena.c4j.ContractReference;

@ContractReference(NorthEastFloatingWindowSpecContract.class)
public interface NorthEastFloatingWindowSpec extends Window {
	
	void move(Vector vector);
	
}