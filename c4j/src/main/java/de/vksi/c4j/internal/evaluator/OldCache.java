package de.vksi.c4j.internal.evaluator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.vksi.c4j.error.ContractError;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.types.Pair;

public class OldCache {
	public static final StaticCall oldRetrieve = new StaticCall(OldCache.class, "oldRetrieve");
	public static final StaticCall oldStore = new StaticCall(OldCache.class, "oldStore");
	public static final String oldStoreDescriptor = "(Ljava/lang/Object;Ljava/lang/Class;I)V";
	public static final StaticCall oldStoreException = new StaticCall(OldCache.class, "oldStoreException");

	private static class OldIdentifier extends Pair<Class<?>, Integer> {
		public OldIdentifier(Class<?> contractClass, int index) {
			super(contractClass, index);
		}
	}

	private static class OldValue extends Pair<Boolean, Object> {
		public OldValue(boolean isException, Object value) {
			super(isException, value);
		}
	}

	private static final Logger logger = Logger.getLogger(OldCache.class);

	private static final ThreadLocal<Deque<Map<OldIdentifier, OldValue>>> oldCache = new ThreadLocal<Deque<Map<OldIdentifier, OldValue>>>() {
		@Override
		protected Deque<Map<OldIdentifier, OldValue>> initialValue() {
			return new ArrayDeque<Map<OldIdentifier, OldValue>>();
		}
	};

	public static int getOldStoreSize() {
		return oldCache.get().size();
	}

	public static Object oldRetrieve(Class<?> contractClass, int index) {
		OldValue oldPair = getCurrentOldCache().get(new OldIdentifier(contractClass, index));
		if (oldPair.getFirst().booleanValue()) {
			return retrieveException(index, (Throwable) oldPair.getSecond());
		} else {
			return retrieveValue(index, oldPair.getSecond());
		}
	}

	private static Object retrieveValue(int index, Object value) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldRetrieve for index '" + index + "' with " + oldCache.get().size() + " returning " + value);
		}
		return value;
	}

	private static Object retrieveException(int index, Throwable exception) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldRetrieve for index '" + index + "' with " + oldCache.get().size()
					+ " returning EXCEPTION " + exception);
		}
		throw new ContractError("Contract Error, old statement #" + index
				+ " has thrown exception when evaluating the expression at the beginning of the method.", exception);
	}

	private static Map<OldIdentifier, OldValue> getCurrentOldCache() {
		return oldCache.get().peekFirst();
	}

	public static void oldStore(Object value, Class<?> contractClass, int index) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldStore for index '" + index + "' with " + oldCache.get().size() + " storing " + value);
		}
		getCurrentOldCache().put(new OldIdentifier(contractClass, index), new OldValue(false, value));
	}

	public static void oldStoreException(Object exception, Class<?> contractClass, int index) {
		if (logger.isTraceEnabled()) {
			logger.trace("oldStore for index '" + index + "' with " + oldCache.get().size() + " storing EXCEPTION "
					+ exception);
		}
		getCurrentOldCache().put(new OldIdentifier(contractClass, index), new OldValue(true, exception));
	}

	public static void add() {
		oldCache.get().addFirst(new HashMap<OldIdentifier, OldValue>());
	}

	public static void remove() {
		oldCache.get().removeFirst();
	}

}
