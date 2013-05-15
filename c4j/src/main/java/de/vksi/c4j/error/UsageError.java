package de.vksi.c4j.error;

import javassist.CannotCompileException;
import javassist.CtBehavior;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.ConstructorExp;
import de.vksi.c4j.internal.compiler.ThrowExp;
import de.vksi.c4j.internal.compiler.ValueExp;

/**
 * Thrown if a C4J feature was used incorrectly. Please consult the error message and the Javadoc to figure out the
 * problem.
 */
public class UsageError extends Error {
	private static final long serialVersionUID = 2689387273644009418L;

	private static final Logger LOGGER = Logger.getLogger(UsageError.class);

	private String message;

	public UsageError(String msg) {
		super(msg);
		this.message = msg;
		LOGGER.error(msg, this);
	}

	public void insertThrowExp(CtBehavior behavior) throws CannotCompileException {
		new ThrowExp(new ConstructorExp(UsageError.class, new ValueExp(message))).insertBefore(behavior);
	}
}
