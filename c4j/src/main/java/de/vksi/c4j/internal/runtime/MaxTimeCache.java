package de.vksi.c4j.internal.runtime;

import java.util.ArrayDeque;
import java.util.Deque;

import de.vksi.c4j.internal.compiler.StaticCall;

public class MaxTimeCache {
	public static final StaticCall setStartTime = new StaticCall(MaxTimeCache.class, "setStartTime");

	private static final ThreadLocal<Deque<Long>> maxTimeCache = new ThreadLocal<Deque<Long>>() {
		@Override
		protected Deque<Long> initialValue() {
			return new ArrayDeque<Long>();
		}
	};

	public static void add() {
		maxTimeCache.get().addFirst(Long.valueOf(0));
	}

	public static void remove() {
		maxTimeCache.get().removeFirst();
	}

	public static void setStartTime() {
		maxTimeCache.get().removeFirst();
		maxTimeCache.get().addFirst(Long.valueOf(System.nanoTime()));
	}

	public static boolean isWithinMaxTime(double seconds) {
		long endTime = System.nanoTime();
		long startTime = maxTimeCache.get().getFirst();
		return ((double) (endTime - startTime)) / 1000000000 <= seconds;
	}
}
