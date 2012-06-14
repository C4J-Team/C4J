package de.vksi.c4j.acceptancetest.floatingwindow;

import de.vksi.c4j.ContractReference;

@ContractReference(NorthEastFloatingWindowSpecContract.class)
public interface NorthEastFloatingWindowSpec extends Window {
	
	void move(Vector vector);
	
}