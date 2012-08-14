package de.vksi.c4j.internal;

import org.apache.log4j.Logger;

import de.vksi.c4j.ContractError;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.configuration.ContractViolationAction;
import de.vksi.c4j.internal.evaluator.Evaluator;

public class ContractErrorHandler {
	public static final StaticCall handleContractException = new StaticCall(ContractErrorHandler.class,
			"handleContractException");

	private static final Logger logger = Logger.getLogger(ContractErrorHandler.class);

	public static void handleContractException(ContractErrorSource source, Throwable throwable, Class<?> affectedClass) {
		if (throwable instanceof AssertionError) {
			contractAction(source, getEnhancedAssertionError(source, (AssertionError) throwable), affectedClass);
			return;
		}
		UsageError usageError = getUsageError(throwable);
		if (usageError != null) {
			contractAction(source, usageError, affectedClass);
		}
		contractAction(source, new ContractError("Contract Error in " + source.getName() + ".", throwable),
				affectedClass);
	}

	private static UsageError getUsageError(Throwable throwable) {
		if (throwable instanceof UsageError) {
			return (UsageError) throwable;
		}
		if (throwable.getCause() == null) {
			return null;
		}
		return getUsageError(throwable.getCause());
	}

	private static void contractAction(ContractErrorSource source, Error error, Class<?> affectedClass) {
		Evaluator.setException(error);
		ContractViolationAction contractViolationActions = RootTransformer.INSTANCE.getXmlConfiguration()
				.getContractViolationAction(affectedClass);
		if (contractViolationActions.isLog().booleanValue()) {
			logger.error("Contract Violation in " + source.getName() + ".", error);
		}
		if (contractViolationActions.isThrowError().booleanValue()) {
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
