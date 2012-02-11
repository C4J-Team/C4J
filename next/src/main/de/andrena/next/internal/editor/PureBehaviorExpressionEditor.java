package de.andrena.next.internal.editor;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.andrena.next.AllowPureAccess;
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
import de.andrena.next.internal.util.PureInspector;

public class PureBehaviorExpressionEditor extends ExprEditor {

	private Logger logger = Logger.getLogger(getClass());
	private CtBehavior affectedBehavior;
	private RootTransformer rootTransformer;
	private PureInspector pureInspector;
	private boolean allowOwnStateChange;
	// necessary to work around bug https://issues.jboss.org/browse/JASSIST-149
	private boolean exceptionThrown;

	public PureBehaviorExpressionEditor(CtBehavior affectedBehavior, RootTransformer rootTransformer,
			PureInspector pureInspector, boolean allowOwnStateChange) {
		this.affectedBehavior = affectedBehavior;
		this.rootTransformer = rootTransformer;
		this.pureInspector = pureInspector;
		this.allowOwnStateChange = allowOwnStateChange;
	}

	protected void replaceWithPureCheck(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		BooleanExp unpureConditions;
		boolean methodIsStatic = Modifier.isStatic(affectedBehavior.getModifiers());
		if (methodIsStatic) {
			unpureConditions = BooleanExp.FALSE;
		} else {
			unpureConditions = new CompareExp(NestedExp.CALLING_OBJECT).eq(NestedExp.THIS);
		}
		int i = 1;
		for (CtClass paramType : affectedBehavior.getParameterTypes()) {
			if (!paramType.isPrimitive()) {
				unpureConditions = unpureConditions.or(new CompareExp(NestedExp.CALLING_OBJECT).eq(NestedExp
						.callingArg(i)));
			}
			i++;
		}
		for (CtField field : getAccessibleFields(affectedBehavior)) {
			if (!field.getType().isPrimitive() && (!methodIsStatic || Modifier.isStatic(field.getModifiers()))) {
				unpureConditions = unpureConditions.or(new CompareExp(NestedExp.CALLING_OBJECT).eq(NestedExp
						.field(field)));
			}
		}
		unpureConditions = new CompareExp(NestedExp.CALLING_OBJECT).ne(NestedExp.NULL).and(unpureConditions);
		IfExp unpureCondition = new IfExp(unpureConditions);
		String errorMsg = "illegal method access on unpure method " + method.getLongName() + " in pure method "
				+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber();
		unpureCondition.addIfBody(getThrowable(errorMsg));
		StandaloneExp replacementExp = unpureCondition.append(StandaloneExp.proceed);
		logger.info("possible call to unpure method " + method.getLongName());
		logger.info("puremagic.replacement-code: \n" + replacementExp.getCode());
		replacementExp.replace(methodCall);
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

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	private void editFieldAccess(FieldAccess fieldAccess) throws CannotCompileException, NotFoundException {
		if (exceptionThrown) {
			return;
		}
		CtField field = fieldAccess.getField();
		if (constructorModifyingOwnClass(field)) {
			return;
		}
		if (!fieldAccess.isWriter()) {
			return;
		}
		if (isAllowedOwnStateChange(field)) {
			return;
		}
		if (field.hasAnnotation(AllowPureAccess.class)) {
			return;
		}
		String errorMsg = "illegal field write access on field " + field.getName() + " in pure method "
				+ affectedBehavior.getLongName() + " on line " + fieldAccess.getLineNumber();
		pureError(errorMsg);
	}

	private void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		ThrowExp throwExp = getThrowable(errorMsg);
		logger.info("pure error replacement code: " + throwExp.getCode());
		throwExp.insertBefore(affectedBehavior);
		exceptionThrown = true;
	}

	private boolean isAllowedOwnStateChange(CtMember member) throws NotFoundException {
		return allowOwnStateChange && affectedBehavior.getDeclaringClass().equals(member.getDeclaringClass());
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
		if (exceptionThrown) {
			return;
		}
		CtMethod method = methodCall.getMethod();
		if (isSynthetic(method)) {
			return;
		}
		if (constructorModifyingOwnClass(method)) {
			return;
		}
		if (rootTransformer.getConfigurationManager().getWhitelistMethods(affectedBehavior.getDeclaringClass())
				.contains(method)) {
			return;
		}
		if (method.hasAnnotation(Pure.class)) {
			return;
		}
		if (Modifier.isStatic(method.getModifiers())) {
			String errorMsg = "illegal method access on static method " + method.getLongName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber();
			pureError(errorMsg);
			return;
		}
		if (pureInspector.inspect(rootTransformer.getInvolvedTypeInspector().inspect(method.getDeclaringClass()),
				method) != null) {
			return;
		}
		replaceWithPureCheck(methodCall);
	}

	private boolean constructorModifyingOwnClass(CtMember member) {
		return affectedBehavior instanceof CtConstructor
				&& member.getDeclaringClass().equals(affectedBehavior.getDeclaringClass());
	}

	private boolean isSynthetic(CtBehavior behavior) {
		return (AccessFlag.of(behavior.getModifiers()) & AccessFlag.SYNTHETIC) > 0;
	}
}
