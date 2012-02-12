package de.andrena.next.internal.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.compiler.ArrayExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.editor.PureBehaviorExpressionEditor;
import de.andrena.next.internal.evaluator.PureEvaluator;

public class PureInspector {
	private Logger logger = Logger.getLogger(getClass());
	private RootTransformer rootTransformer;

	public PureInspector(RootTransformer rootTransformer) {
		this.rootTransformer = rootTransformer;
	}

	public CtBehavior inspect(ListOrderedSet<CtClass> involvedClasses, CtBehavior behavior) {
		for (CtClass involvedClass : involvedClasses) {
			CtBehavior involvedBehavior = getInvolvedBehavior(behavior, involvedClass);
			if (involvedBehavior != null && involvedBehavior.hasAnnotation(Pure.class)) {
				return involvedBehavior;
			}
		}
		return null;
	}

	private CtBehavior getInvolvedBehavior(CtBehavior affectedBehavior, CtClass involvedClass) {
		if (affectedBehavior instanceof CtConstructor) {
			try {
				return involvedClass.getDeclaredConstructor(affectedBehavior.getParameterTypes());
			} catch (NotFoundException e) {
				return null;
			}
		}
		try {
			return involvedClass.getDeclaredMethod(affectedBehavior.getName(), affectedBehavior.getParameterTypes());
		} catch (NotFoundException e) {
			return null;
		}
	}

	public void verify(CtBehavior affectedBehavior, boolean allowOwnStateChange) throws CannotCompileException,
			NotFoundException {
		affectedBehavior.instrument(new PureBehaviorExpressionEditor(affectedBehavior, rootTransformer, this,
				allowOwnStateChange));
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
		if (!Modifier.isStatic(affectedBehavior.getModifiers())
				&& rootTransformer.getConfigurationManager().isWithinRootPackages(affectedBehavior.getDeclaringClass())) {
			StandaloneExp checkUnpureAccessExp = new StaticCallExp(PureEvaluator.checkUnpureAccess, NestedExp.THIS)
					.toStandalone();
			logger.info("puremagic.checkUnpureAccess insertBefore " + affectedBehavior.getLongName() + ": \n"
					+ checkUnpureAccessExp.getCode());
			checkUnpureAccessExp.insertBefore(affectedBehavior);
		}
	}
}
