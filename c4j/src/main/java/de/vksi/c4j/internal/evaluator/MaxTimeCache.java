package de.vksi.c4j.internal.evaluator;

import java.util.HashMap;
import java.util.Map;

import de.vksi.c4j.internal.compiler.StaticCall;

public class MaxTimeCache {
	public static final StaticCall setStartTime = new StaticCall(MaxTimeCache.class, "setStartTime");

	private static final ThreadLocal<Map<Integer, Long>> maxTimeCache = new ThreadLocal<Map<Integer, Long>>() {
		@Override
		protected Map<Integer, Long> initialValue() {
			return new HashMap<Integer, Long>();
		}
	};

	public static void setStartTime() {
		int length = Thread.currentThread().getStackTrace().length;
		maxTimeCache.get().put(Integer.valueOf(length),
				Long.valueOf(System.nanoTime()));
	}

	public static boolean isWithinMaxTime(double seconds) {
		long endTime = System.nanoTime();
		int length = Thread.currentThread().getStackTrace().length;
		long startTime = maxTimeCache.get().get(Integer.valueOf(length - 1));
		return ((double) (endTime - startTime)) / 1000000000 <= seconds;
	}
}
