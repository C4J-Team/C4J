package de.andrena.next.internal.editor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;

import org.apache.log4j.Logger;

import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.compiler.BooleanExp;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.CompareExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
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
			checkConstructor(constructorCall, constructorCall.where(), constructorCall.getConstructor(),
					constructorCall.getLineNumber());
		} catch (Exception e) {
			throw new CannotCompileException(e);
		}
	}

	protected void checkConstructor(Expr expression, CtBehavior affectedBehavior, CtConstructor constructor,
			int lineNumber) throws CannotCompileException, SecurityException, NoSuchMethodException, NotFoundException {
		if (constructor.isEmpty()) {
			if (constructor.getDeclaringClass().isFrozen())
				new PureConstructorExpressionEditor(affectedBehavior, rootTransformer, pureInspectorProvider).doit(
						constructor.getDeclaringClass(), constructor.getMethodInfo2());
			return;
		}
		if (rootTransformer.getConfiguration().getWhitelistConstructors().contains(constructor)) {
			return;
		}
		if (constructor.hasAnnotation(Pure.class)) {
			return;
		}
		replaceWithPureCheck(expression, constructor);
	}

	protected void replaceWithPureCheck(Expr expression, CtBehavior behavior) throws NotFoundException,
			CannotCompileException {
		BooleanExp unpureConditions = new CompareExp(NestedExp.CALLING_OBJECT).eq(NestedExp.THIS);
		int i = 1;
		for (CtClass paramType : affectedBehavior.getParameterTypes()) {
			if (!paramType.isPrimitive()) {
				unpureConditions = unpureConditions.or(new CompareExp(NestedExp.CALLING_OBJECT).eq(NestedExp
						.callingArg(i)));
			}
			i++;
		}
		for (CtField field : getAccessibleFields(affectedBehavior)) {
			if (!field.getType().isPrimitive()) {
				unpureConditions = unpureConditions.or(new CompareExp(NestedExp.CALLING_OBJECT).eq(NestedExp
						.field(field)));
			}
		}
		IfExp unpureCondition = new IfExp(unpureConditions);
		String errorMsg = "illegal method access on unpure method/constructor " + behavior.getLongName()
				+ " in pure method/constructor " + affectedBehavior.getLongName() + " on line "
				+ expression.getLineNumber();
		unpureCondition.addIfBody(getThrowable(errorMsg));
		StandaloneExp replacementExp = unpureCondition.append(StandaloneExp.proceed);
		logger.info("puremagic.replacement-code: \n" + replacementExp.getCode());
		replacementExp.replace(expression);
	}

	private Set<CtField> getAccessibleFields(CtBehavior affectedBehavior) {
		Set<CtField> accessibleFields = new HashSet<CtField>();
		Collections.addAll(accessibleFields, affectedBehavior.getDeclaringClass().getFields());
		Collections.addAll(accessibleFields, affectedBehavior.getDeclaringClass().getDeclaredFields());
		return accessibleFields;
	}

	protected ThrowExp getThrowable(String errorMsg) throws CannotCompileException {
		return new ThrowExp(new ConstructorExp(AssertionError.class, new CastExp(Object.class, new ValueExp(errorMsg))));
	}
}
