package de.andrena.c4j.internal.util;

public class CollectionsHelper {
	public boolean arrayContains(Object[] array, Object needle) {
		for (Object arrayItem : array) {
			if (arrayItem.equals(needle)) {
				return true;
			}
		}
		return false;
	}
}
