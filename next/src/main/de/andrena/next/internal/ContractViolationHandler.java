package de.andrena.next.internal;

import de.andrena.next.internal.compiler.StaticCall;

public class ContractViolationHandler {
	public static final StaticCall handleContractException = new StaticCall(ContractViolationHandler.class,
			"handleContractException");

	public static void handleContractException(Throwable exception) {
		if (exception instanceof AssertionError) {
			throw (AssertionError) exception;
		} else {
			throw new ContractException(exception);
		}
	}
}
