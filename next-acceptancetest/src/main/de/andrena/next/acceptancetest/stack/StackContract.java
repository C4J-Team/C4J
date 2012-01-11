package de.andrena.next.acceptancetest.stack;

import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import de.andrena.next.ClassInvariant;
import de.andrena.next.Condition;

public class StackContract<T> extends Stack<T> {

	private Stack<T> target = Condition.target();

	public StackContract(int capacity) {
		super(capacity);
		if (pre()) {
			assert capacity > 0 : "capacity > 0";
		}
		if (post()) {
			assert target.capacity() == capacity : "capacity set";
		}
	}

	@ClassInvariant
	public void classInvariant() {
		// no class invariant identified yet
	}

}
