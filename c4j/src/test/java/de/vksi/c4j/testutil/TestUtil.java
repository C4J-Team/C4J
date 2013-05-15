package de.vksi.c4j.testutil;

import java.lang.ref.WeakReference;

public class TestUtil {
	public static void forceGarbageCollection() {
		Object obj = new Object();
		WeakReference<Object> ref = new WeakReference<Object>(obj);
		obj = null;
		while (ref.get() != null) {
			System.gc();
		}
	}
}
