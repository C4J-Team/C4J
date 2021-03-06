package de.vksi.c4j.acceptancetest.workingstudent;

import de.vksi.c4j.ContractReference;

@ContractReference(WorkingStudentContract.class)
public class WorkingStudent implements EmployeeSpec, StudentSpec {

	int age;
	
	@Override
	public String getMatriculationNumber() {
		return "987654321";
	}

	@Override
	public String getEmployerName() {
		return "andrena objects ag";
	}

	@Override
	public void setAge(int age) {
		this.age = age;
	}
	
	public int getAge() {
		return age;
	}

}
