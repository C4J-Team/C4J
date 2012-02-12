package de.andrena.next.systemtest;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.rules.Verifier;

import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.transformer.TransformationException;

public class TransformerAwareRule extends Verifier {
	private String expectedLogMessage;
	private Level expectedLogLevel;
	private Level bannedLogLevel;
	private String bannedLogMessage;
	private Class<?> expectedException;

	private static Map<String, Level> logMap = new HashMap<String, Level>();

	public void expectLogWarning(String message) {
		expectLog(Level.WARN, message);
	}

	public void expectLog(Level level, String message) {
		expectedLogLevel = level;
		expectedLogMessage = message;
	}

	public void banLogWarning(String message) {
		banLog(Level.WARN, message);
	}

	public void banLog(Level level, String message) {
		bannedLogLevel = level;
		bannedLogMessage = message;
	}

	public void expectTransformationException(String message) {
		expectLog(Level.ERROR, message);
		expectedException = TransformationException.class;
	}

	@Override
	protected void verify() throws Throwable {
		try {
			verifyException();
			verifyLog();
		} finally {
			expectedException = null;
			expectedLogLevel = null;
			expectedLogMessage = null;
			bannedLogLevel = null;
			bannedLogMessage = null;
			RootTransformer.resetLastException();
		}
	}

	private void verifyLog() throws Exception {
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
			logMap.put(event.getMessage().toString(), event.getLevel());
		}
	}
}
