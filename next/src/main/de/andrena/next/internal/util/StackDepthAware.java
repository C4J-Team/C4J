package de.andrena.next.internal.util;

import java.util.HashMap;
import java.util.Map;

public abstract class StackDepthAware<T extends Map<?, ?>> {
	private Map<Integer, T> map = new HashMap<Integer, T>();

	protected abstract T initialValue();

	public T get(Integer stackTraceDepth) {
		if (map.get(stackTraceDepth) == null) {
			map.put(stackTraceDepth, initialValue());
		}
		return map.get(stackTraceDepth);
	}

	public int size() {
		int size = 0;
		for (Map<?, ?> entry : map.values()) {
			size += entry.size();
		}
		return size;
	}
}
