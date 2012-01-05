package de.andrena.next.acceptancetest.stack;

import java.util.ArrayList;

import de.andrena.next.Contract;

@Contract(StackContract.class)
public class Stack<T> implements StackSpec<T> {

	private int capacity;
	private ArrayList<T> values;

	public Stack(int capacity) {
		this.capacity = capacity;
		values = new ArrayList<T>();
	}

	public int capacity() {
		int result = 0;
		result = capacity;
		return result;
	}

	public int count() {
		int result = 0;
		result = values.size();
		return result;
	}

	public void push(T x) {
		values.add(x);
	}

	public void pop() {
		values.remove(count() - 1);
	}

	public T top() {
		T result = null;
		result = values.get(values.size() - 1);
		return result;
	}

	public boolean isFull() {
		boolean result = false;
		result = count() == capacity;
		return result;
	}

	public boolean isEmpty() {
		boolean result = false;
		result = values.isEmpty();
		return result;
	}

	public T get(int index) {
		T result = null;
		result = values.get(index);
		return result;
	}

}
