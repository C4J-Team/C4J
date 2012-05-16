package de.andrena.c4j.internal;

import javassist.CannotCompileException;
import javassist.CtBehavior;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.compiler.ConstructorExp;
import de.andrena.c4j.internal.compiler.ThrowExp;
import de.andrena.c4j.internal.compiler.ValueExp;

public class RTFMException extends CannotCompileException {
	private static final long serialVersionUID = 2689387273644009418L;

	private Logger logger = Logger.getLogger(RTFMException.class);

	private String message;

	public RTFMException(String msg) {
		super(msg);
		this.message = msg;
		logger.error(msg, this);
	}

	public void insertThrowExp(CtBehavior behavior) throws CannotCompileException {
		new ThrowExp(new ConstructorExp(ContractError.class, new ValueExp(message))).insertBefore(behavior);
	}
}
