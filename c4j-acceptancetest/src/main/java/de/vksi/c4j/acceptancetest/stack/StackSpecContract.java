package de.vksi.c4j.acceptancetest.stack;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static de.vksi.c4j.Condition.result;
import de.vksi.c4j.Target;

public class StackSpecContract<T> implements StackSpec<T> {

	@Target
	private StackSpec<T> target;

	private Object[] old_values;

	@Override
	public int capacity() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			int result = result(Integer.class);
			assert result > 0 : "result > 0";
		}
		return (Integer) ignored();
	}

	@Override
	public int count() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			int result = result(Integer.class);
			assert result >= 0 : "result >= 0";
			assert result <= target.capacity() : "count <= capacity";
		}
		return (Integer) ignored();
	}

	@Override
	public void push(T x) {
		if (preCondition()) {
			assert x != null : "x != null";
			assert !target.isFull() : "not isFull";
		}
		if (postCondition()) {
			assert target.count() == old(target.count()) + 1 : "old count increased by 1";
			assert target.top() == x : "x set";
		}
	}

	@Override
	public void pop() {
		if (preCondition()) {
			assert !target.isEmpty() : "not isEmpty";
			if (target.count() > 1) {
				old_values = new Object[target.count() - 1];
				for (int i = 0; i < old_values.length; i = i + 1) {
					old_values[i] = target.get(i);
				}
			}
		}
		if (postCondition()) {
			assert target.count() == old(target.count()) - 1 : "old count decreased by 1";
			if (!target.isEmpty()) {
				for (int i = 0; i < old_values.length; i = i + 1) {
					assert old_values[i] == target.get(i) : "values unchanged";
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T top() {
		if (preCondition()) {
			assert !target.isEmpty() : "not isEmpty";
		}
		if (postCondition()) {
			T result = (T) result();
			assert result == target.get(target.count() - 1) : "result == top_item";
		}
		return ignored();
	}

	@Override
	public boolean isFull() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			boolean result = result(Boolean.class);
			if (result) {
				assert target.count() == target.capacity() : "count == capacity";
			} else {
				assert target.count() < target.capacity() : "count < capacity";
			}
		}
		return (Boolean) ignored();
	}

	@Override
	public boolean isEmpty() {
		if (preCondition()) {
			// no pre-condition identified yet
		}
		if (postCondition()) {
			boolean result = result(Boolean.class);
			if (result) {
				assert target.count() == 0 : "count == 0";
			} else {
				assert target.count() > 0 : "count > 0";
			}
		}
		return (Boolean) ignored();
	}

	@Override
	public T get(int index) {
		if (preCondition()) {
			assert index >= 0 : "index >= 0";
			assert index < target.count() : "index < count";
		}
		if (postCondition()) {
			// no post-condition identified yet
		}
		return ignored();
	}

}
