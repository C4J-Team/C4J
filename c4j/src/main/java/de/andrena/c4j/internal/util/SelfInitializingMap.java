package de.andrena.c4j.internal.util;

import java.util.Collection;

public abstract class SelfInitializingMap<S, T extends Collection<?>> extends AbstractSelfInitializingMap<S, T> {
	@Override
	public int size() {
		int size = 0;
		for (Collection<?> entry : map.values()) {
			size += entry.size();
		}
		return size;
	}

	public boolean contains(Object obj) {
		for (Collection<?> entry : map.values()) {
			if (entry.contains(obj)) {
				return true;
			}
		}
		return false;
	}
}