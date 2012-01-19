package de.andrena.next.acceptancetest.workingstudent;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.pre;

public final class StudentSpecContract implements StudentSpec {

	@Override
	public String getMatriculationNumber() {
		// No contracts identified yet
		return ignored();
	}

	@Override
	public void setAge(int age) {
		if (pre()) {
			assert age > 0 : "age > 0";
			assert age < 100 : "age < 100";
		}
	}
	
}