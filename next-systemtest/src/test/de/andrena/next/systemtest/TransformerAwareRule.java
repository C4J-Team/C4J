package de.andrena.next.systemtest;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.rules.Verifier;

import de.andrena.next.internal.RootTransformer;

public class TransformerAwareRule extends Verifier {
	private String expectedLogMessage;
	private Level expectedLogLevel;
	private Level bannedLogLevel;
	private String bannedLogMessage;

	private static Map<String, Level> logMap = new HashMap<String, Level>();

	public void expectLogWarning(String message) {
		expectLog(Level.WARN, message);
	}

	public void expectLogError(String message) {
		expectLog(Level.ERROR, message);
	}

	public void expectLog(Level level, String message) {
		expectedLogLevel = level;
		expectedLogMessage = message;
	}

	public void banLogWarning(String message) {
		banLog(Level.WARN, message);
	}

	public void banLogError(String message) {
		banLog(Level.ERROR, message);
	}

	public void banLog(Level level, String message) {
		bannedLogLevel = level;
		bannedLogMessage = message;
	}

	@Override
	protected void verify() throws Throwable {
		if (RootTransformer.getLastException() != null) {
			throw RootTransformer.getLastException();
		}
		String logMessage = expectedLogMessage;
		Level logLevel = expectedLogLevel;
		expectedLogMessage = null;
		expectedLogLevel = null;
		if (logMessage != null && (!logMap.containsKey(logMessage) || !logMap.get(logMessage).equals(logLevel))) {
			throw new Exception("expected " + logLevel + " with message '" + logMessage + "'");
		}
		logMessage = bannedLogMessage;
		logLevel = bannedLogLevel;
		bannedLogMessage = null;
		bannedLogLevel = null;
		if (logMessage != null && logMap.containsKey(logMessage) && logMap.get(logMessage).equals(logLevel)) {
			throw new Exception("noticed banned " + logLevel + " with message '" + logMessage + "'");
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
