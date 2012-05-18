package de.andrena.c4j.internal.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMapper<S, T> {
	private boolean useNativeHashcode;
	private Map<Integer, List<EntryMap>> map = new HashMap<Integer, List<EntryMap>>();
	private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
	private Map<Reference<Object>, EntryMap> referenceMap = new HashMap<Reference<Object>, EntryMap>();

	public ObjectMapper() {
		this(true);
	}

	ObjectMapper(boolean useNativeHashcode) {
		this.useNativeHashcode = useNativeHashcode;
	}

	public T get(Object obj, S key) {
		EntryMap entry = getEntry(obj);
		if (entry != null) {
			return entry.map.get(key);
		}
		return null;
	}

	private EntryMap getEntry(Object obj) {
		List<EntryMap> hashList = map.get(getHashCode(obj));
		if (hashList == null || hashList.size() == 0) {
			return null;
		}
		for (EntryMap elem : hashList) {
			if (elem.object.get() == obj) {
				return elem;
			}
		}
		return null;
	}

	private Integer getHashCode(Object obj) {
		if (useNativeHashcode) {
			return Integer.valueOf(System.identityHashCode(obj));
		} else {
			return Integer.valueOf(obj.hashCode());
		}
	}

	public void put(Object obj, S key, T val) {
		cleanup();
		if (getEntry(obj) != null) {
			getEntry(obj).map.put(key, val);
			return;
		}
		Integer hashCode = getHashCode(obj);
		if (!map.containsKey(hashCode)) {
			map.put(hashCode, new ArrayList<EntryMap>());
		}
		WeakReference<Object> weakReference = new WeakReference<Object>(obj, referenceQueue);
		EntryMap entryMap = new EntryMap(weakReference, key, val, hashCode);
		referenceMap.put(weakReference, entryMap);
		map.get(hashCode).add(entryMap);
	}

	public boolean contains(Object obj, S key) {
		if (getEntry(obj) != null) {
			return getEntry(obj).map.get(key) != null;
		}
		return false;
	}

	public void cleanup() {
		Reference<? extends Object> removableReference = null;
		while ((removableReference = referenceQueue.poll()) != null) {
			EntryMap removableEntry = referenceMap.get(removableReference);
			map.get(removableEntry.hashCode).remove(removableEntry);
			if (map.get(removableEntry.hashCode).isEmpty()) {
				map.remove(removableEntry.hashCode);
			}
		}
	}

	public long size() {
		long size = 0;
		for (List<EntryMap> hashCodeList : map.values()) {
			size += hashCodeList.size();
		}
		return size;
	}

	private class EntryMap {
		private WeakReference<Object> object;
		private Map<S, T> map;
		private Integer hashCode;

		private EntryMap(WeakReference<Object> object, S key, T value, Integer hashCode) {
			this.object = object;
			this.map = new HashMap<S, T>();
			map.put(key, value);
			this.hashCode = hashCode;
		}
	}
}
