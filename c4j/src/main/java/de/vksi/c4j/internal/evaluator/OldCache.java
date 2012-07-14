package de.vksi.c4j.internal.evaluator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.vksi.c4j.ContractError;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.util.Pair;
import de.vksi.c4j.internal.util.SelfInitializingMapOfMaps;

public class OldCache {
	public static final StaticCall oldRetrieve = new StaticCall(OldCache.class, "oldRetrieve");
	public static final StaticCall oldStore = new StaticCall(OldCache.class, "oldStore");
	public static final StaticCall oldStoreException = new StaticCall(OldCache.class, "oldStoreException");

	private static final Logger logger = Logger.getLogger(OldCache.class);

	/**
	 * Integer = stack trace depth, Integer = old-call index, Boolean = exception thrown
	 */
	static final ThreadLocal<Integer> currentOldCacheEnvironment = new ThreadLocal<Integer>();
	private static final ThreadLocal<SelfInitializingMapOfMaps<Integer, Map<Integer, Pair<Boolean, Object>>>> oldCache = new ThreadLocal<SelfInitializingMapOfMaps<Integer, Map<Integer, Pair<Boolean, Object>>>>() {
		@Override
		protected SelfInitializingMapOfMaps<Integer, Map<Integer, Pair<Boolean, Object>>> initialValue() {
			return new SelfInitializingMapOfMaps<Integer, Map<Integer, Pair<Boolean, Object>>>() {
				@Override
				protected Map<Integer, Pair<Boolean, Object>> initialValue() {
					return new HashMap<Integer, Pair<Boolean, Object>>();
				}
			};
		}
	};

	public static int getOldStoreSize() {
		return oldCache.get().size();
	}

	public static Object oldRetrieve(int index) {
		Pair<Boolean, Object> oldPair = getCurrentOldCache().get(Integer.valueOf(index));
		if (oldPair.getFirst().booleanValue()) {
			return retrieveException(index, (Throwable) oldPair.getSecond());
		} else {
			return retrieveValue(index, oldPair.getSecond());
		}
	}

	private static Object retrieveValue(int index, Object value) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldRetrieve for index '" + index + "' with " + currentOldCacheEnvironment.get()
					+ " returning " + value);
		}
		return value;
	}

	private static Object retrieveException(int index, Throwable exception) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldRetrieve for index '" + index + "' with " + currentOldCacheEnvironment.get()
					+ " returning EXCEPTION " + exception);
		}
		throw new ContractError("Contract Error, old statement #" + index
				+ " has thrown exception when evaluating the expression at the beginning of the method.", exception);
	}

	private static Map<Integer, Pair<Boolean, Object>> getCurrentOldCache() {
		return oldCache.get().get(currentOldCacheEnvironment.get());
	}

	public static void oldStore(Object value, int index) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldStore for index '" + index + "' with "
					+ currentOldCacheEnvironment.get()
					+ " storing " + value);
		}
		getCurrentOldCache().put(index, new Pair<Boolean, Object>(Boolean.FALSE, value));
	}

	public static void oldStoreException(Object exception, int index) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldStore for index '" + index + "' with "
					+ currentOldCacheEnvironment.get()
					+ " storing EXCEPTION " + exception);
		}
		getCurrentOldCache().put(index, new Pair<Boolean, Object>(Boolean.TRUE, exception));
	}

	public static void setCurrentEnvironment(int stackTraceDepth) {
		currentOldCacheEnvironment.set(Integer.valueOf(stackTraceDepth));
	}

	public static void clear(int stackTraceDepth) {
		oldCache.get().get(Integer.valueOf(stackTraceDepth)).clear();
	}

}
