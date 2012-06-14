package de.vksi.c4j.internal.evaluator;

import de.vksi.c4j.internal.util.ObjectMapper;
import de.vksi.c4j.internal.util.Pair;

public class ContractCache {
	private static final ObjectMapper<Pair<Class<?>, Class<?>>, Object> contractCache = new ObjectMapper<Pair<Class<?>, Class<?>>, Object>();

	public static Object getContractFromCache(Object target, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		if (target == null) {
			return null;
		}
		Object contract;
		Pair<Class<?>, Class<?>> classPair = new Pair<Class<?>, Class<?>>(contractClass, callingClass);
		if (contractCache.contains(target, classPair)) {
			contract = contractCache.get(target, classPair);
		} else {
			contract = contractClass.newInstance();
			contractCache.put(target, classPair, contract);
		}
		return contract;
	}
}
