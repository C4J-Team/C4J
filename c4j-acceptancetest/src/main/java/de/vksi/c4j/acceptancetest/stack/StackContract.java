package de.vksi.c4j.acceptancetest.stack;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.Target;

public class StackContract<T> extends Stack<T> {

	@Target
	private Stack<T> target;

	public StackContract(int capacity) {
		super(capacity);
		if (preCondition()) {
			assert capacity > 0 : "capacity > 0";
		}
		if (postCondition()) {
			assert target.capacity() == capacity : "capacity set";
		}
	}

	@ClassInvariant
	public void classInvariant() {
		// no class invariant identified yet
	}

}
