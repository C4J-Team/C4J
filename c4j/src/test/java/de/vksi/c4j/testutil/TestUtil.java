package de.vksi.c4j.testutil;

import java.lang.ref.WeakReference;

public class TestUtil {
	public static void waitForGarbageCollection(WeakReference<?> weakReference) {
		while (weakReference.get() != null) {
			System.gc();
		}
	}
}
