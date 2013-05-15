package de.vksi.c4j.error;

import org.apache.log4j.Logger;

/**
 * Thrown if a C4J feature was used incorrectly. Please consult the error message and the Javadoc to figure out the
 * problem.
 */
public class UsageError extends Error {
	private static final long serialVersionUID = 2689387273644009418L;

	private static final Logger LOGGER = Logger.getLogger(UsageError.class);

	public UsageError(String msg) {
		super(msg);
		LOGGER.error(msg, this);
	}
}
