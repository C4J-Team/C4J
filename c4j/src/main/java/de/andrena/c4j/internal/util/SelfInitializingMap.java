package de.andrena.c4j.internal.util;

import java.util.HashMap;
import java.util.Map;

public abstract class SelfInitializingMap<S, T extends Map<?, ?>> {
	private Map<S, T> map = new HashMap<S, T>();

	protected abstract T initialValue();

	public T get(S key) {
		if (map.get(key) == null) {
			map.put(key, initialValue());
		}
		return map.get(key);
	}

	public int size() {
		int size = 0;
		for (Map<?, ?> entry : map.values()) {
			size += entry.size();
		}
		return size;
	}
}
