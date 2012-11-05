package de.vksi.c4j.internal.util;

public class CollectionsHelper {
	public static boolean arrayContains(Object[] array, Object needle) {
		for (Object arrayItem : array) {
			if (arrayItem.equals(needle)) {
				return true;
			}
		}
		return false;
	}
}
