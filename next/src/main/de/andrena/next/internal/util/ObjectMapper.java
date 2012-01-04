package de.andrena.next.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMapper<S, T> {
	private boolean useNativeHashcode;
	private Map<Integer, List<EntryMap<S, T>>> map = new HashMap<Integer, List<EntryMap<S, T>>>();

	public ObjectMapper() {
		this(true);
	}

	protected ObjectMapper(boolean useNativeHashcode) {
		this.useNativeHashcode = useNativeHashcode;
	}

	public T get(Object obj, S key) {
		EntryMap<S, T> entry = getEntry(obj);
		if (entry != null) {
			return entry.map.get(key);
		}
		return null;
	}

	private EntryMap<S, T> getEntry(Object obj) {
		List<EntryMap<S, T>> hashList = map.get(getHashCode(obj));
		if (hashList == null || hashList.size() == 0) {
			return null;
		}
		for (EntryMap<S, T> elem : hashList) {
			if (elem.object == obj) {
				return elem;
			}
		}
		return null;
	}

	private Integer getHashCode(Object obj) {
		if (useNativeHashcode) {
			return System.identityHashCode(obj);
		} else {
			return obj.hashCode();
		}
	}

	public void put(Object obj, S key, T val) {
		if (getEntry(obj) != null) {
			getEntry(obj).map.put(key, val);
			return;
		}
		if (!map.containsKey(getHashCode(obj))) {
			map.put(getHashCode(obj), new ArrayList<EntryMap<S, T>>());
		}
		map.get(getHashCode(obj)).add(new EntryMap<S, T>(obj, key, val));
	}

	public boolean contains(Object obj, S key) {
		if (getEntry(obj) != null) {
			return getEntry(obj).map.get(key) != null;
		}
		return false;
	}

	private class EntryMap<U, V> {
		private Object object;
		private Map<U, V> map;

		private EntryMap(Object object, U key, V value) {
			this.object = object;
			this.map = new HashMap<U, V>();
			map.put(key, value);
		}
	}
}
