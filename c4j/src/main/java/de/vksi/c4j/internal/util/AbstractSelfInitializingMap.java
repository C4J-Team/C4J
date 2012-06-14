package de.vksi.c4j.internal.util;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSelfInitializingMap<S, T> {
	protected final Map<S, T> map = new HashMap<S, T>();

	protected abstract T initialValue();

	public T get(S key) {
		if (map.get(key) == null) {
			map.put(key, initialValue());
		}
		return map.get(key);
	}

	public abstract int size();
}