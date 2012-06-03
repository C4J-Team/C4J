package de.andrena.c4j.internal.util;

import java.util.Map;

public abstract class SelfInitializingMapOfMaps<S, T extends Map<?, ?>> extends AbstractSelfInitializingMap<S, T> {
	public int size() {
		int size = 0;
		for (Map<?, ?> entry : map.values()) {
			size += entry.size();
		}
		return size;
	}
}
