package de.vksi.c4j.internal.evaluator;

import de.vksi.c4j.internal.util.ObjectMapper;
import de.vksi.c4j.internal.util.Pair;

public class ContractCache {
	private static final ObjectMapper<ContractCacheEntry, Object> contractCache = new ObjectMapper<ContractCacheEntry, Object>();

	private static class ContractCacheEntry extends Pair<Class<?>, Class<?>> {
		public ContractCacheEntry(Class<?> contractClass, Class<?> callingClass) {
			super(contractClass, callingClass);
		}

	}

	public static Object getContractFromCache(Object target, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		if (target == null) {
			return null;
		}
		Object contract;
		ContractCacheEntry classPair = new ContractCacheEntry(contractClass, callingClass);
		if (contractCache.contains(target, classPair)) {
			contract = contractCache.get(target, classPair);
		} else {
			contract = contractClass.newInstance();
			contractCache.put(target, classPair, contract);
		}
		return contract;
	}
}
