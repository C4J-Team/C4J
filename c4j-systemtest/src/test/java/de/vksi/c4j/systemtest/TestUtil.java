package de.vksi.c4j.systemtest;

import java.lang.ref.WeakReference;

public class TestUtil {
	public static void waitForGarbageCollection(WeakReference<?> weakReference) {
		while (weakReference.get() != null) {
			System.gc();
		}
	}

	/**
	 * Windows is very inaccurate with Thread.sleep, not guaranteeing that at least the given amount of time has passed.
	 */
	public static void waitAtLeast(double seconds) throws InterruptedException {
		long startTime = System.nanoTime();
		Thread.sleep((long) (seconds * 1000));
		while ((System.nanoTime() - startTime) < (seconds * 1000000000)) {
			Thread.sleep(10);
		}
	}
}
