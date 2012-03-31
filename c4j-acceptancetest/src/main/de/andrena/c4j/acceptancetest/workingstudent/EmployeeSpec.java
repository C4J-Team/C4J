package de.andrena.c4j.acceptancetest.workingstudent;

import de.andrena.c4j.ContractReference;

@ContractReference(EmployeeSpecContract.class)
public interface EmployeeSpec {
	
	String getEmployerName();
	
	void setAge(int age);
	
}