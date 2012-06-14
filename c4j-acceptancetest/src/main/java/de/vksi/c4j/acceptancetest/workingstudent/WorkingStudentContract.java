package de.vksi.c4j.acceptancetest.workingstudent;

import static de.vksi.c4j.Condition.preCondition;

public final class WorkingStudentContract extends WorkingStudent {

	@Override
	public void setAge(int age) {
		if(preCondition()) {
			assert age > 0;
		}
	}
	
}
