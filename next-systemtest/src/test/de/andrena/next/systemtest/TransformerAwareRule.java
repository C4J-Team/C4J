package de.andrena.next.systemtest;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.transformer.TransformationException;

public class TransformerAwareRule implements TestRule {
	private String expectedLogMessage;
	private Level expectedLogLevel;
	private Level bannedLogLevel;
	private String bannedLogMessage;
	private Class<?> expectedException;
	private Map<String, Level> usedLogMap;

	private static Map<String, Level> globalLogMap = new HashMap<String, Level>();
	private static Map<String, Level> localLogMap = new HashMap<String, Level>();

	public void expectGlobalLog(Level level, String message) {
		usedLogMap = globalLogMap;
		expectedLogLevel = level;
		expectedLogMessage = message;
	}

	public void banGlobalLog(Level level, String message) {
		usedLogMap = globalLogMap;
		bannedLogLevel = level;
		bannedLogMessage = message;
	}

	public void expectLocalLog(Level level, String message) {
		usedLogMap = localLogMap;
		expectedLogLevel = level;
		expectedLogMessage = message;
	}

	public void banLocalLog(Level level, String message) {
		usedLogMap = localLogMap;
		bannedLogLevel = level;
		bannedLogMessage = message;
	}

	public void expectTransformationException(String message) {
		expectGlobalLog(Level.ERROR, message);
		expectedException = TransformationException.class;
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				localLogMap.clear();
				usedLogMap = null;
				expectedException = null;
				expectedLogLevel = null;
				expectedLogMessage = null;
				bannedLogLevel = null;
				bannedLogMessage = null;
				RootTransformer.resetLastException();
				base.evaluate();
				verify();
			}
		};
	}

	protected void verify() throws Throwable {
		verifyException();
		verifyLog(usedLogMap);
	}

	private void verifyLog(Map<String, Level> logMap) throws Exception {
		if (expectedLogMessage != null
				&& (!logMap.containsKey(expectedLogMessage) || !logMap.get(expectedLogMessage).equals(expectedLogLevel))) {
			throw new Exception("expected " + expectedLogLevel + " with message '" + expectedLogMessage + "'");
		}
		if (bannedLogMessage != null && logMap.containsKey(bannedLogMessage)
				&& logMap.get(bannedLogMessage).equals(bannedLogLevel)) {
			throw new Exception("noticed banned " + bannedLogLevel + " with message '" + bannedLogMessage + "'");
		}
	}

	private void verifyException() throws Exception, Throwable {
		if (expectedException != null) {
			if (RootTransformer.getLastException() == null
					|| !RootTransformer.getLastException().getClass().equals(expectedException)) {
				throw new Exception("expected a " + expectedException.getName() + " to be thrown");
			}
		} else if (RootTransformer.getLastException() != null) {
			throw RootTransformer.getLastException();
		}
	}

	public static class RuleAppender extends AppenderSkeleton {
		@Override
		public void close() {
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		@Override
		protected void append(LoggingEvent event) {
			globalLogMap.put(event.getMessage().toString(), event.getLevel());
			localLogMap.put(event.getMessage().toString(), event.getLevel());
		}
	}
}
