package de.andrena.c4j.internal;

import java.util.Set;

import org.apache.log4j.Logger;

import de.andrena.c4j.Configuration.ContractViolationAction;
import de.andrena.c4j.internal.compiler.StaticCall;

public class ContractErrorHandler {
	public static final StaticCall handleContractException = new StaticCall(ContractErrorHandler.class,
			"handleContractException");

	private static final Logger logger = Logger.getLogger(ContractErrorHandler.class);

	public static void handleContractException(ContractErrorSource source, Throwable throwable, Class<?> affectedClass) {
		if (throwable instanceof AssertionError) {
			contractAction(source, getEnhancedAssertionError(source, (AssertionError) throwable), affectedClass);
		} else {
			contractAction(source, new ContractError("Contract Error in " + source.getName() + ".", throwable),
					affectedClass);
		}
	}

	private static void contractAction(ContractErrorSource source, Error error, Class<?> affectedClass) {
		Set<ContractViolationAction> contractViolationActions = RootTransformer.INSTANCE.getConfigurationManager()
				.getConfiguration(affectedClass).getContractViolationActions();
		if (contractViolationActions.contains(ContractViolationAction.LOG)) {
			logger.error("Contract Violation in " + source.getName() + ".", error);
		}
		if (contractViolationActions.contains(ContractViolationAction.ASSERTION_ERROR)) {
			throw error;
		}
	}

	private static AssertionError getEnhancedAssertionError(ContractErrorSource source, AssertionError error) {
		AssertionError enhancedError = new AssertionError(getPaddedMessage(error.getMessage()) + "(" + source.getName()
				+ ")");
		enhancedError.initCause(error.getCause());
		enhancedError.setStackTrace(error.getStackTrace());
		return enhancedError;
	}

	private static String getPaddedMessage(String oldMessage) {
		if (oldMessage != null && !oldMessage.isEmpty()) {
			return oldMessage + " ";
		}
		return "";
	}

	public enum ContractErrorSource {
		PRE_CONDITION("pre-condition"), POST_CONDITION("post-condition"), CLASS_INVARIANT("class-invariant");

		private String name;

		private ContractErrorSource(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
