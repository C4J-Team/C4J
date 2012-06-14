package de.vksi.c4j.acceptancetest.workingstudent;

import static de.vksi.c4j.Condition.pre;

public final class YoungWorkingStudentContract extends YoungWorkingStudent {

	@Override
	public void setAge(int age) {
		if(pre()) {
			assert age < 50 : "age < 50";
		}
	}
	
}
