package de.andrena.c4j.acceptancetest.workingstudent;

import de.andrena.next.Contract;

@Contract(EmployeeSpecContract.class)
public interface EmployeeSpec {
	
	String getEmployerName();
	
	void setAge(int age);
	
}