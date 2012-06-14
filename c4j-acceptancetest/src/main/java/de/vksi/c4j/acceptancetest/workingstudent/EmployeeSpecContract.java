package de.vksi.c4j.acceptancetest.workingstudent;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.preCondition;

public final class EmployeeSpecContract implements EmployeeSpec {

	@Override
	public String getEmployerName() {
		// No contracts identified yet
		return ignored();
	}

	@Override
	public void setAge(int age) {
		if(preCondition()) {
			assert age > 0 : "age > 0";
			assert age < 100 : "age < 100";
		}
		
	}
	
}
