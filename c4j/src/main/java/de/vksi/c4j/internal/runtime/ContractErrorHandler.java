package de.vksi.c4j.internal.runtime;

import org.apache.log4j.Logger;

import de.vksi.c4j.error.ContractError;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.configuration.ContractViolationAction;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;

public class ContractErrorHandler {
	public static final StaticCall handleContractException = new StaticCall(ContractErrorHandler.class,
			"handleContractException");
	public static final StaticCall handlePreConditionException = new StaticCall(ContractErrorHandler.class,
			"handlePreConditionException");
	public static final StaticCall handlePreConditionSuccess = new StaticCall(ContractErrorHandler.class,
			"handlePreConditionSuccess");

	private static final Logger logger = Logger.getLogger(ContractErrorHandler.class);

	private static ThreadLocal<PreConditionResult> preconditionResult = new ThreadLocal<PreConditionResult>() {
		@Override
		protected PreConditionResult initialValue() {
			return new PreConditionResult();
		};
	};

	public static void handlePreConditionException(ContractErrorSource source, Throwable throwable,
			Class<?> affectedClass, boolean lastCall) {
		Evaluator.setException(throwable);
		preconditionResult.get().registerError(throwable);
		if (preconditionResult.get().hasSuccess()) {
			handlePreConditionLspViolation(source, affectedClass);
			return;
		}
		if (lastCall) {
			handleContractException(source, throwable, affectedClass);
		}
	}

	public static void handlePreConditionSuccess(ContractErrorSource source, Class<?> affectedClass) {
		preconditionResult.get().registerSuccess();
		if (preconditionResult.get().hasError()) {
			handlePreConditionLspViolation(source, affectedClass);
			return;
		}
	}

	private static void handlePreConditionLspViolation(ContractErrorSource source, Class<?> affectedClass) {
		AssertionError lspError = new AssertionError(
				"LSP Violation: Invalid multiple inheritance, as two pre-conditions returned different results.");
		handleContractException(source, lspError, affectedClass);
	}

	public static void handleContractException(ContractErrorSource source, Throwable throwable, Class<?> affectedClass) {
		Evaluator.setException(throwable);
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
		ContractViolationAction contractViolationActions = XmlConfigurationManager.INSTANCE
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
		PRE_CONDITION("pre-condition"), POST_CONDITION("post-condition"), CLASS_INVARIANT("class-invariant"), INITIALIZER(
				"initializer");

		private String name;

		private ContractErrorSource(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static void resetPreConditionResults() {
		preconditionResult.remove();
	}
}
