package de.andrena.next.acceptancetest.subinterfaces;

import de.andrena.next.Contract;

@Contract(TopContract.class)
public interface Top {
	
	int a(String parameter);

}
