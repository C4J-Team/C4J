package de.andrena.next.internal.editor;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;

import org.apache.log4j.Logger;

import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.ThrowExp;
import de.andrena.next.internal.compiler.ValueExp;
import de.andrena.next.internal.util.PureInspectorProvider;

public class PureConstructorExpressionEditor extends ExprEditor {
	protected Logger logger = Logger.getLogger(getClass());
	protected CtBehavior affectedBehavior;
	protected RootTransformer rootTransformer;
	protected PureInspectorProvider pureInspectorProvider;

	public PureConstructorExpressionEditor(CtBehavior affectedBehavior, RootTransformer rootTransformer,
			PureInspectorProvider pureInspectorProvider) {
		this.affectedBehavior = affectedBehavior;
		this.rootTransformer = rootTransformer;
		this.pureInspectorProvider = pureInspectorProvider;
	}

	@Override
	public void edit(ConstructorCall constructorCall) throws CannotCompileException {
		try {
			checkConstructor(constructorCall.where(), constructorCall.getConstructor(), constructorCall.getLineNumber());
		} catch (Exception e) {
			throw new CannotCompileException(e);
		}
	}

	protected void checkConstructor(CtBehavior affectedBehavior, CtConstructor constructor, int lineNumber)
			throws CannotCompileException, SecurityException, NoSuchMethodException, NotFoundException {
		if (constructor.isEmpty()) {
			if (constructor.getDeclaringClass().isFrozen())
				new PureConstructorExpressionEditor(affectedBehavior, rootTransformer, pureInspectorProvider).doit(
						constructor.getDeclaringClass(), constructor.getMethodInfo2());
			return;
		}
		if (rootTransformer.getConfiguration().getWhitelistConstructors().contains(constructor)) {
			return;
		}
		if (!constructor.hasAnnotation(Pure.class)) {
			pureError("illegal constructor access on constructor " + constructor.getLongName()
					+ " in pure method/constructor " + affectedBehavior.getLongName() + " on line " + lineNumber);
		}
	}

	protected void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class, new ValueExp(errorMsg))))
				.insertBefore(affectedBehavior);
	}
}
