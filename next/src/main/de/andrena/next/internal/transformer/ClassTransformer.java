package de.andrena.next.internal.transformer;

import org.apache.log4j.Logger;

import de.andrena.next.internal.ContractInfo;

public abstract class ClassTransformer {
	protected Logger logger = Logger.getLogger(getClass());

	public abstract void transform(ContractInfo contract) throws Exception;
}
