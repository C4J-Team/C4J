package de.andrena.c4j.internal;

import java.util.Set;

import org.apache.log4j.Logger;

import de.andrena.c4j.Configuration.ContractViolationAction;
import de.andrena.c4j.internal.compiler.StaticCall;

public class ContractViolationHandler {
	public static final StaticCall handleContractException = new StaticCall(ContractViolationHandler.class,
			"handleContractException");

	private static final Logger logger = Logger.getLogger(ContractViolationHandler.class);

	public static void handleContractException(Throwable throwable, Class<?> affectedClass) {
		if (throwable instanceof AssertionError) {
			contractAction((AssertionError) throwable, affectedClass);
		} else {
			contractAction(new ContractError(throwable), affectedClass);
		}
	}

	private static void contractAction(Error error, Class<?> affectedClass) {
		Set<ContractViolationAction> contractViolationActions = RootTransformer.INSTANCE.getConfigurationManager()
				.getConfiguration(affectedClass).getContractViolationActions();
		if (contractViolationActions.contains(ContractViolationAction.LOG)) {
			logger.error("Contract Violation.", error);
		}
		if (contractViolationActions.contains(ContractViolationAction.ASSERTION_ERROR)) {
			throw error;
		}
	}
}
