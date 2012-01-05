package de.andrena.next.acceptancetest.stack;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.old;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static de.andrena.next.Condition.result;
import de.andrena.next.ThreadSafe;

public class StackSpecContract<T> implements StackSpec<T> {

	@ThreadSafe
	private Object[] old_values;

	@Override
	public int capacity() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			assert result > 0 : "result > 0";
		}
		return ignored();
	}

	@Override
	public int count() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= capacity() : "count <= capacity";
		}
		return ignored();
	}

	@Override
	public void push(T x) {
		if (pre()) {
			assert x != null : "x != null";
			assert !isFull() : "not isFull";
		}
		if (post()) {
			assert count() == old(count()) + 1 : "old count increased by 1";
			assert top() == x : "x set";
		}
	}

	@Override
	public void pop() {
		if (pre()) {
			assert !isEmpty() : "not isEmpty";
			if (count() > 1) {
				old_values = new Object[count() - 1];
				for (int i = 0; i < old_values.length; i = i + 1) {
					old_values[i] = get(i);
				}
			}
		}
		if (post()) {
			assert count() == old(count()) - 1 : "old count decreased by 1";
			if (!isEmpty()) {
				for (int i = 0; i < old_values.length; i = i + 1) {
					assert old_values[i] == get(i) : "values unchanged";
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T top() {
		if (pre()) {
			assert !isEmpty() : "not isEmpty";
		}
		if (post()) {
			T result = (T) result();
			assert result == get(count() - 1) : "result == top_item";
		}
		return ignored();
	}

	@Override
	public boolean isFull() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			boolean result = result(Boolean.class);
			if (result) {
				assert count() == capacity() : "count == capacity";
			}
		}
		return ignored();
	}

	@Override
	public boolean isEmpty() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			boolean result = result(Boolean.class);
			if (result) {
				assert count() == 0 : "count == 0";
			}
		}
		return ignored();
	}

	@Override
	public T get(int index) {
		if (pre()) {
			assert index >= 0 : "index >= 0";
			assert index < count() : "index < count";
		}
		if (post()) {
			// no post-condition identified yet
		}
		return ignored();
	}

}
