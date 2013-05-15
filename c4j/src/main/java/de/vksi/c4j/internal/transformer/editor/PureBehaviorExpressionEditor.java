package de.vksi.c4j.internal.transformer.editor;

import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.Pure;
import de.vksi.c4j.internal.compiler.CastExp;
import de.vksi.c4j.internal.compiler.ConstructorExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.compiler.ThrowExp;
import de.vksi.c4j.internal.compiler.ValueExp;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;
import de.vksi.c4j.internal.runtime.PureEvaluator;
import de.vksi.c4j.internal.runtime.PureEvaluator.ErrorType;
import de.vksi.c4j.internal.transformer.util.InvolvedTypeInspector;
import de.vksi.c4j.internal.types.ListOrderedSet;

public class PureBehaviorExpressionEditor extends ExprEditor {

	private static final String C4J_INTERNAL_PACKAGE = "de.vksi.c4j.internal";
	private Logger logger = Logger.getLogger(getClass());
	private CtMethod affectedMethod;
	private PureInspector pureInspector;
	private boolean allowOwnStateChange;
	// necessary to work around bug https://issues.jboss.org/browse/JASSIST-149
	private ThrowExp pureError = null;
	private InvolvedTypeInspector involvedTypeInspector = new InvolvedTypeInspector();

	public PureBehaviorExpressionEditor(CtMethod affectedMethod, PureInspector pureInspector,
			boolean allowOwnStateChange) throws CannotCompileException {
		this.affectedMethod = affectedMethod;
		this.pureInspector = pureInspector;
		this.allowOwnStateChange = allowOwnStateChange;
	}

	public ThrowExp getPureError() {
		return pureError;
	}

	private void replaceWithPureCheck(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		StaticCall checkMethod = PureEvaluator.checkExternalAccess;
		if (XmlConfigurationManager.INSTANCE.getConfiguration(affectedMethod.getDeclaringClass()).getBlacklistMethods()
				.contains(method)) {
			checkMethod = PureEvaluator.checkExternalBlacklistAccess;
		}
		StandaloneExp checkUnpureAccessExp = new StaticCallExp(checkMethod, NestedExp.CALLING_OBJECT, new ValueExp(
				method.getLongName())).toStandalone();
		StandaloneExp replacementExp = checkUnpureAccessExp.append(StandaloneExp.PROCEED_AND_ASSIGN);
		if (logger.isDebugEnabled()) {
			logger.debug("possible call to external unpure method " + method.getLongName());
		}
		if (logger.isTraceEnabled()) {
			logger.trace("replacement-code: " + replacementExp.getCode());
		}
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
		if (!fieldAccess.isWriter()) {
			return;
		}
		CtField field = fieldAccess.getField();
		if (isAllowedOwnStateChange(field)) {
			return;
		}
		if (field.hasAnnotation(AllowPureAccess.class)) {
			return;
		}
		pureError("illegal write access on field " + field.getName() + " in pure method "
				+ affectedMethod.getLongName() + " on line " + fieldAccess.getLineNumber());
	}

	private void pureError(String errorMsg) throws CannotCompileException {
		logger.error(errorMsg);
		pureError = getThrowable(errorMsg);
		if (logger.isTraceEnabled()) {
			logger.trace("pure error replacement code: " + pureError.getCode());
		}
	}

	private boolean isAllowedOwnStateChange(CtMember member) throws NotFoundException {
		return allowOwnStateChange && affectedMethod.getDeclaringClass().equals(member.getDeclaringClass());
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
		CtMethod method;
		try {
			method = methodCall.getMethod();
		} catch (NotFoundException e) {
			// if referencing another contract method, handled in ContractMethodExpressionEditor.editMethodCall
			return;
		}
		if (isSynthetic(method)) {
			return;
		}
		if (XmlConfigurationManager.INSTANCE.getConfiguration(affectedMethod.getDeclaringClass()).getWhitelistMethods()
				.contains(method)) {
			return;
		}
		if (method.hasAnnotation(Pure.class)) {
			return;
		}
		if (Modifier.isStatic(method.getModifiers())) {
			editStaticMethodCall(methodCall, method);
			return;
		}
		editNonStaticMethodCall(methodCall, method);
	}

	private void editNonStaticMethodCall(MethodCall methodCall, CtMethod method) throws NotFoundException,
			CannotCompileException {
		ListOrderedSet<CtClass> involvedTypes = involvedTypeInspector.inspect(method.getDeclaringClass());
		ListOrderedSet<ContractInfo> contracts = ContractRegistry.INSTANCE.getContractsForTypes(involvedTypes, method
				.getDeclaringClass());
		if (pureInspector.getPureOrigin(involvedTypes, contracts, method) != null) {
			return;
		}
		if (XmlConfigurationManager.INSTANCE.isWithinRootPackages(method.getDeclaringClass())) {
			return;
		}
		replaceWithPureCheck(methodCall);
	}

	private void editStaticMethodCall(MethodCall methodCall, CtMethod method) throws CannotCompileException {
		if (method.getDeclaringClass().getPackageName().startsWith(C4J_INTERNAL_PACKAGE)) {
			return;
		}
		if (XmlConfigurationManager.INSTANCE.isWithinRootPackages(method.getDeclaringClass())
				|| XmlConfigurationManager.INSTANCE.getConfiguration(affectedMethod.getDeclaringClass())
						.getBlacklistMethods().contains(method)) {
			pureError("illegal access on static method " + method.getLongName() + " in pure method "
					+ affectedMethod.getLongName() + " on line " + methodCall.getLineNumber());
			return;
		}
		PureEvaluator.warnExternalAccess(method.getLongName(), ErrorType.UNPURE);
	}

	private boolean isSynthetic(CtBehavior behavior) {
		return (AccessFlag.of(behavior.getModifiers()) & AccessFlag.SYNTHETIC) > 0;
	}
}
