package de.andrena.c4j.acceptancetest.workingstudent;

import de.andrena.c4j.ContractReference;

@ContractReference(StudentSpecContract.class)
public interface StudentSpec {
	
	String getMatriculationNumber();
	
	void setAge(int age);
	
}
