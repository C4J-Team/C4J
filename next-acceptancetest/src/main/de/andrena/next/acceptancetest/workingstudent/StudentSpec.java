package de.andrena.next.acceptancetest.workingstudent;

import de.andrena.next.Contract;

@Contract(StudentSpecContract.class)
public interface StudentSpec {
	
	String getMatriculationNumber();
	
	void setAge(int age);
	
}
