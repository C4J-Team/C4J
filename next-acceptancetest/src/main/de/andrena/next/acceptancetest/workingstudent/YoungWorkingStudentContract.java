package de.andrena.next.acceptancetest.workingstudent;

import static de.andrena.next.Condition.pre;

public final class YoungWorkingStudentContract extends YoungWorkingStudent {

	@Override
	public void setAge(int age) {
		if(pre()) {
			assert age < 50 : "age < 50";
		}
	}
	
}
