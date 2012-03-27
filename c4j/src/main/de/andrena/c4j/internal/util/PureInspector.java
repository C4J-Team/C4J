package de.andrena.c4j.internal.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.c4j.Pure;
import de.andrena.c4j.PureTarget;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.compiler.ArrayExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;
import de.andrena.c4j.internal.compiler.StaticCallExp;
import de.andrena.c4j.internal.editor.PureBehaviorExpressionEditor;
import de.andrena.c4j.internal.editor.UnpureBehaviorExpressionEditor;
import de.andrena.c4j.internal.evaluator.PureEvaluator;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class PureInspector {
	private Logger logger = Logger.getLogger(getClass());
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private UnpureBehaviorExpressionEditor unpureBehaviorExpressionEditor = new UnpureBehaviorExpressionEditor();
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();

	public CtMethod getPureOrigin(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtMethod method) {
		for (CtClass involvedClass : involvedClasses) {
			CtMethod involvedMethod = getInvolvedMethod(method, involvedClass);
			if (involvedMethod != null && involvedMethod.hasAnnotation(Pure.class)) {
				return involvedMethod;
			}
		}
		for (ContractInfo contract : contracts) {
			CtMethod contractMethod = affectedBehaviorLocator.getContractMethod(contract, method);
			if (contractMethod != null && contractMethod.hasAnnotation(PureTarget.class)) {
				return contractMethod;
			}
		}
		return null;
	}

	private CtMethod getInvolvedMethod(CtMethod affectedBehavior, CtClass involvedClass) {
		try {
			return involvedClass.getDeclaredMethod(affectedBehavior.getName(), affectedBehavior.getParameterTypes());
		} catch (NotFoundException e) {
			return null;
		}
	}

	public void verifyUnchangeable(CtBehavior affectedBehavior,
			ListOrderedSet<ContractInfo> contracts) throws CannotCompileException {
		List<NestedExp> unpureObjects = new ArrayList<NestedExp>();
		for (ContractInfo contract : contracts) {
			List<NestedExp> contractUnchangeables = contract.getUnchangeables().get(
						affectedBehavior.getName() + affectedBehavior.getSignature());
			if (contractUnchangeables != null) {
				logger.debug("reading unchangeables from " + affectedBehavior.getName()
						+ affectedBehavior.getSignature());
				unpureObjects.addAll(contractUnchangeables);
			}
		}
		if (!unpureObjects.isEmpty()) {
			registerUnpureObjects(affectedBehavior, unpureObjects);
		}
	}

	public void verify(CtBehavior affectedBehavior, boolean allowOwnStateChange)
			throws CannotCompileException,
			NotFoundException {
		PureBehaviorExpressionEditor editor = new PureBehaviorExpressionEditor(affectedBehavior, rootTransformer, this,
						allowOwnStateChange);
		affectedBehavior.instrument(editor);
		if (editor.getPureError() != null) {
			editor.getPureError().insertBefore(affectedBehavior);
		}
		List<NestedExp> unpureObjects = new ArrayList<NestedExp>();
		boolean methodIsStatic = Modifier.isStatic(affectedBehavior.getModifiers());
		if (!methodIsStatic) {
			unpureObjects.add(NestedExp.THIS);
		}
		int i = 1;
		for (CtClass paramType : affectedBehavior.getParameterTypes()) {
			if (!paramType.isPrimitive()) {
				unpureObjects.add(NestedExp.arg(i));
			}
			i++;
		}
		for (CtField field : getAccessibleFields(affectedBehavior)) {
			if (!field.getType().isPrimitive() && (!methodIsStatic || Modifier.isStatic(field.getModifiers()))) {
				unpureObjects.add(NestedExp.field(field));
			}
		}
		if (!unpureObjects.isEmpty()) {
			registerUnpureObjects(affectedBehavior, unpureObjects);
		}
	}

	private void registerUnpureObjects(CtBehavior affectedBehavior, List<NestedExp> unpureObjects)
			throws CannotCompileException {
		ArrayExp unpureArray = new ArrayExp(Object.class, unpureObjects);
		StandaloneExp registerUnpureExp = new StaticCallExp(PureEvaluator.registerUnpure, unpureArray).toStandalone();
		StandaloneExp unregisterUnpureExp = new StaticCallExp(PureEvaluator.unregisterUnpure, unpureArray)
				.toStandalone();
		logger.info("puremagic.insertBefore " + affectedBehavior.getLongName() + ": \n" + registerUnpureExp.getCode());
		logger.info("puremagic.insertFinally " + affectedBehavior.getLongName() + ": \n"
				+ unregisterUnpureExp.getCode());
		registerUnpureExp.insertBefore(affectedBehavior);
		unregisterUnpureExp.insertFinally(affectedBehavior);
	}

	private Set<CtField> getAccessibleFields(CtBehavior affectedBehavior) {
		Set<CtField> accessibleFields = new HashSet<CtField>();
		Collections.addAll(accessibleFields, affectedBehavior.getDeclaringClass().getFields());
		Collections.addAll(accessibleFields, affectedBehavior.getDeclaringClass().getDeclaredFields());
		return accessibleFields;
	}

	public void checkUnpureAccess(CtBehavior affectedBehavior) throws CannotCompileException {
		if (!rootTransformer.getConfigurationManager().isWithinRootPackages(affectedBehavior.getDeclaringClass())) {
			return;
		}
		affectedBehavior.instrument(unpureBehaviorExpressionEditor);
		if (Modifier.isStatic(affectedBehavior.getModifiers())) {
			return;
		}
		StandaloneExp checkUnpureAccessExp = new StaticCallExp(PureEvaluator.checkUnpureAccess, NestedExp.THIS)
				.toStandalone();
		logger.info("puremagic.checkUnpureAccess insertBefore " + affectedBehavior.getLongName() + ": \n"
				+ checkUnpureAccessExp.getCode());
		checkUnpureAccessExp.insertBefore(affectedBehavior);
	}
}
