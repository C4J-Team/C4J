package de.andrena.next.acceptancetest.subinterfaces;

import de.andrena.next.Contract;

@Contract(TopContract.class)
public interface Top {
	
	int pre(String parameter);
	
	int post(String parameter);
	
	int preAndPost(String parameter);
	
	int invariant(String parameter);
	
	int unchanged();
	
	int old();

}
