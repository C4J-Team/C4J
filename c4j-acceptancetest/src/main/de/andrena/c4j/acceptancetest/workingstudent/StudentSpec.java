package de.andrena.c4j.acceptancetest.workingstudent;

import de.andrena.next.Contract;

@Contract(StudentSpecContract.class)
public interface StudentSpec {
	
	String getMatriculationNumber();
	
	void setAge(int age);
	
}
