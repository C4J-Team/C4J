package de.andrena.next.internal.util;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.andrena.next.Pure;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.compiler.AssignmentExp;
import de.andrena.next.internal.compiler.EmptyExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.editor.PureBehaviorExpressionEditor;

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
		StandaloneExp paramAssignments = new EmptyExp();
		int i = 1;
		for (CtClass paramType : affectedBehavior.getParameterTypes()) {
			if (!paramType.isPrimitive()) {
				affectedBehavior.addLocalVariable(NestedExp.callingArg(i).toString(), paramType);
				paramAssignments = paramAssignments
						.append(new AssignmentExp(NestedExp.callingArg(i), NestedExp.arg(i)));
			}
			i++;
		}
		logger.info("puremagic.insertBefore " + affectedBehavior.getLongName() + ": \n" + paramAssignments.getCode());
		paramAssignments.insertBefore(affectedBehavior);
		affectedBehavior.instrument(new PureBehaviorExpressionEditor(affectedBehavior, rootTransformer, this,
				allowOwnStateChange));
	}
}
