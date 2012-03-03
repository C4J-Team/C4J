package de.andrena.c4j.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Set containing Objects. Instead of relying on hashCode() for equality, this set relies on the == operator. It also
 * discards null values and counts, how many times an object has been added to the set. Only after it is removed the
 * same number of times it is being fully removed from this Set.
 */
public class ObjectIdentitySet implements Set<Object> {
	private Map<Integer, List<Entry>> map = new HashMap<Integer, List<Entry>>();

	@Override
	public int size() {
		int size = 0;
		for (List<Entry> list : map.values()) {
			size += list.size();
		}
		return size;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		Integer hashCode = getHashCode(o);
		if (!map.containsKey(hashCode)) {
			return false;
		}
		for (Entry entry : map.get(hashCode)) {
			if (entry.getObject() == o) {
				return true;
			}
		}
		return false;
	}

	private Integer getHashCode(Object o) {
		return Integer.valueOf(System.identityHashCode(o));
	}

	@Override
	public Iterator<Object> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	private Entry getEntry(Object o) {
		Integer hashCode = getHashCode(o);
		if (!map.containsKey(hashCode)) {
			return null;
		}
		for (Entry entry : map.get(hashCode)) {
			if (entry.getObject() == o) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public boolean add(Object e) {
		if (e == null) {
			return false;
		}
		Entry entry = getEntry(e);
		if (entry != null) {
			entry.incrementCount();
			return false;
		}
		Integer hashCode = getHashCode(e);
		if (!map.containsKey(hashCode)) {
			map.put(hashCode, new ArrayList<Entry>());
		}
		map.get(hashCode).add(new Entry(e));
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if (!contains(o)) {
			return false;
		}
		Integer hashCode = getHashCode(o);
		for (Iterator<Entry> iterator = map.get(hashCode).iterator(); iterator.hasNext();) {
			Entry entry = iterator.next();
			if (entry.getObject() == o) {
				entry.decrementCount();
				if (entry.canBeRemoved()) {
					iterator.remove();
					if (map.get(hashCode).isEmpty()) {
						map.remove(hashCode);
					}
					return true;
				}
				break;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		boolean changed = false;
		for (Object o : c) {
			if (add(o)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object o : c) {
			if (remove(o)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		map.clear();
	}

	private class Entry {
		private Object object;
		private int count;

		public Entry(Object object) {
			this.object = object;
			count = 1;
		}

		public void incrementCount() {
			count++;
		}

		public void decrementCount() {
			count--;
		}

		public boolean canBeRemoved() {
			return count <= 0;
		}

		public Object getObject() {
			return object;
		}
	}

}
