package de.andrena.c4j.internal.editor;

import java.lang.reflect.Modifier;

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

import de.andrena.c4j.AllowPureAccess;
import de.andrena.c4j.Pure;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.CastExp;
import de.andrena.c4j.internal.compiler.ConstructorExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCall;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.compiler.ThrowExp;
import de.andrena.c4j.internal.compiler.ValueExp;
import de.andrena.c4j.internal.evaluator.PureEvaluator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;
import de.andrena.c4j.internal.util.InvolvedTypeInspector;
import de.andrena.c4j.internal.util.ListOrderedSet;
import de.andrena.c4j.internal.util.PureInspector;

public class PureBehaviorExpressionEditor extends ExprEditor {

	private Logger logger = Logger.getLogger(getClass());
	private CtBehavior affectedBehavior;
	private RootTransformer rootTransformer;
	private PureInspector pureInspector;
	private boolean allowOwnStateChange;
	// necessary to work around bug https://issues.jboss.org/browse/JASSIST-149
	private boolean exceptionThrown;
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();

	public PureBehaviorExpressionEditor(CtBehavior affectedBehavior, RootTransformer rootTransformer,
			PureInspector pureInspector, boolean allowOwnStateChange) {
		this.affectedBehavior = affectedBehavior;
		this.rootTransformer = rootTransformer;
		this.pureInspector = pureInspector;
		this.allowOwnStateChange = allowOwnStateChange;
	}

	private void replaceWithPureCheck(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		StaticCall checkMethod = PureEvaluator.checkExternalAccess;
		if (rootTransformer.getConfigurationManager().getConfiguration(affectedBehavior.getDeclaringClass())
				.getBlacklistMethods().contains(method)) {
			checkMethod = PureEvaluator.checkExternalBlacklistAccess;
		}
		StandaloneExp checkUnpureAccessExp = new StaticCallExp(checkMethod,
				NestedExp.CALLING_OBJECT, new ValueExp(method.getLongName())).toStandalone();
		StandaloneExp replacementExp = checkUnpureAccessExp.append(StandaloneExp.proceed);
		logger.info("possible call to external unpure method " + method.getLongName());
		logger.info("replacement-code: " + replacementExp.getCode());
		replacementExp.replace(methodCall);
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
		String errorMsg = "illegal write access on field " + field.getName() + " in pure method "
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
		if (rootTransformer.getConfigurationManager().getConfiguration(affectedBehavior.getDeclaringClass())
				.getWhitelistMethods().contains(method)) {
			return;
		}
		if (method.hasAnnotation(Pure.class)) {
			return;
		}
		if (Modifier.isStatic(method.getModifiers())) {
			String errorMsg = "illegal access on static method " + method.getLongName() + " in pure method "
					+ affectedBehavior.getLongName() + " on line " + methodCall.getLineNumber();
			pureError(errorMsg);
			return;
		}
		ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(method.getDeclaringClass());
		ListOrderedSet<ContractInfo> contracts = RootTransformer.INSTANCE.getContractsForTypes(involvedTypes);
		if (pureInspector.getPureOrigin(involvedTypes, contracts, method) != null) {
			return;
		}
		if (rootTransformer.getConfigurationManager().isWithinRootPackages(method.getDeclaringClass())) {
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
