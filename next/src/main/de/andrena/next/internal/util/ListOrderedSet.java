package de.andrena.next.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListOrderedSet<T> implements Set<T> {
	private List<T> list = new ArrayList<T>();
	private Set<T> set = new HashSet<T>();

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <U> U[] toArray(U[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(T e) {
		if (!set.contains(e)) {
			set.add(e);
			list.add(e);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		if (set.contains(o)) {
			set.remove(o);
			list.remove(o);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		for (T item : c) {
			if (add(item)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (T item : this) {
			if (!c.contains(item)) {
				remove(item);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object item : c) {
			if (remove(item)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		set.clear();
		list.clear();
	}

	@Override
	public boolean equals(Object obj) {
		return set.equals(obj);
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

}
