package de.andrena.next.acceptancetest.workingstudent;

import static de.andrena.next.Condition.pre;

public final class WorkingStudentContract extends WorkingStudent {

	@Override
	public void setAge(int age) {
		if(pre()) {
			assert age > 0;
		}
	}
	
}
