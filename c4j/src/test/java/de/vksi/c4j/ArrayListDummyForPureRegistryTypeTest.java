package de.vksi.c4j;

import java.util.ArrayList;

public class ArrayListDummyForPureRegistryTypeTest<T> {
	public int size() {
		return 1;
	}

	public T get(int index) {
		return null;
	}

	protected void removeRange(int a, int b) {

	}

	public String[] toArray() {
		return new String[0];
	}

	public T[] toArray(T[] a) {
		return new ArrayList<T>().toArray(a);
	}

	public boolean add(T element) {
		return false;
	}

	public void add(int index, T element) {

	}

	@SuppressWarnings("unused")
	private void fastRemove(int index) {

	}
}
