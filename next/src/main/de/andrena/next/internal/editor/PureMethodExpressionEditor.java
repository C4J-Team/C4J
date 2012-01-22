package de.andrena.next.internal.editor;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.andrena.next.Configuration;
import de.andrena.next.Pure;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.ValueExp;

public class PureMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	private CtBehavior affectedBehavior;
	private Configuration configuration;

	public PureMethodExpressionEditor(CtBehavior affectedBehavior, Configuration configuration) {
		this.affectedBehavior = affectedBehavior;
		this.configuration = configuration;
	}

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	private void editFieldAccess(FieldAccess fieldAccess) throws CannotCompileException, NotFoundException {
		if (fieldAccess.isWriter()) {
			pureError("illegal field write access on field " + fieldAccess.getField().getName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + fieldAccess.getLineNumber());
		}
	}

	@Override
	public void edit(MethodCall methodCall) throws CannotCompileException {
		try {
			editMethodCall(methodCall);
		} catch (Exception e) {
			throw new CannotCompileException(e);
		}
	}

	private void editMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException,
			SecurityException, NoSuchMethodException {
		CtMethod method = methodCall.getMethod();
		for (Method whitelistMethod : configuration.getPureWhitelist()) {
			if (isEqual(method, whitelistMethod)) {
				return;
			}
		}
		if (!method.hasAnnotation(Pure.class)) {
			pureError("illegal method access on method " + method.getLongName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber());
		}
	}

	private boolean isEqual(CtMethod method, Method whitelistMethod) throws NotFoundException {
		if (!whitelistMethod.getDeclaringClass().getName().equals(method.getDeclaringClass().getName())) {
			logger.info(whitelistMethod.getDeclaringClass().getName() + " vs " + method.getDeclaringClass().getName());
			return false;
		}
		if (!whitelistMethod.getName().equals(method.getName())) {
			logger.info(whitelistMethod.getName() + " vs " + method.getName());
			return false;
		}
		Class<?>[] whitelistParamTypes = whitelistMethod.getParameterTypes();
		if (whitelistParamTypes.length != method.getParameterTypes().length) {
			logger.info(whitelistParamTypes.length + " vs " + method.getParameterTypes().length);
			return false;
		}
		for (int i = 0; i < whitelistParamTypes.length; i++) {
			if (!whitelistParamTypes[i].getName().equals(method.getParameterTypes()[i].getName())) {
				logger.info(whitelistParamTypes[i].getName() + " vs " + method.getParameterTypes()[i].getName());
				return false;
			}
		}
		return true;
	}

	private void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class, new ValueExp(errorMsg))))
				.insertBefore(affectedBehavior);
	}
}
