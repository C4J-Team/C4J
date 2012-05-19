package de.andrena.c4j.acceptancetest.workingstudent;

import static de.andrena.c4j.Condition.pre;

public final class WorkingStudentContract extends WorkingStudent {

	@Override
	public void setAge(int age) {
		if(pre()) {
			assert age > 0;
		}
	}
	
}
