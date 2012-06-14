package de.vksi.c4j.internal.evaluator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.util.SelfInitializingMapOfMaps;

public class OldCache {
	public static final StaticCall oldRetrieve = new StaticCall(OldCache.class, "oldRetrieve");
	public static final StaticCall oldStore = new StaticCall(OldCache.class, "oldStore");

	private static final Logger logger = Logger.getLogger(OldCache.class);

	/**
	 * Integer = stack trace depth, class = contract class
	 */
	static final ThreadLocal<Integer> currentOldCacheEnvironment = new ThreadLocal<Integer>();
	private static final ThreadLocal<SelfInitializingMapOfMaps<Integer, Map<Integer, Object>>> oldCache = new ThreadLocal<SelfInitializingMapOfMaps<Integer, Map<Integer, Object>>>() {
		@Override
		protected SelfInitializingMapOfMaps<Integer, Map<Integer, Object>> initialValue() {
			return new SelfInitializingMapOfMaps<Integer, Map<Integer, Object>>() {
				@Override
				protected Map<Integer, Object> initialValue() {
					return new HashMap<Integer, Object>();
				}
			};
		}
	};

	public static int getOldStoreSize() {
		return oldCache.get().size();
	}

	public static Object oldRetrieve(int index) {
		Object oldValue = getCurrentOldCache().get(Integer.valueOf(index));
		if (logger.isTraceEnabled()) {
			logger.trace("oldRetrieve for index '" + index + "' with " + currentOldCacheEnvironment.get()
					+ " returning " + oldValue);
		}
		return oldValue;
	}

	private static Map<Integer, Object> getCurrentOldCache() {
		return oldCache.get().get(currentOldCacheEnvironment.get());
	}

	public static void oldStore(int index, Object value) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldStore for index '" + index + "' with "
					+ currentOldCacheEnvironment.get()
					+ " storing " + value);
		}
		getCurrentOldCache().put(index, value);
	}

	public static void setCurrentEnvironment(int stackTraceDepth) {
		currentOldCacheEnvironment.set(Integer.valueOf(stackTraceDepth));
	}

	public static void clear(int stackTraceDepth) {
		oldCache.get().get(Integer.valueOf(stackTraceDepth)).clear();
	}

}
