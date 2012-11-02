package de.vksi.c4j.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.Pure;
import de.vksi.c4j.PureTarget;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.compiler.ArrayExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.editor.ArrayAccessEditor;
import de.vksi.c4j.internal.editor.PureBehaviorExpressionEditor;
import de.vksi.c4j.internal.editor.UnpureBehaviorExpressionEditor;
import de.vksi.c4j.internal.evaluator.PureEvaluator;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public class PureInspector {
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;
	private UnpureBehaviorExpressionEditor unpureBehaviorExpressionEditor = new UnpureBehaviorExpressionEditor();
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	private ArrayAccessEditor arrayAccessEditor = new ArrayAccessEditor();

	public CtMethod getPureOrigin(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtMethod method) {
		for (CtClass involvedClass : involvedClasses) {
			CtMethod involvedMethod = getInvolvedMethod(method, involvedClass);
			if (involvedMethod != null
					&& (involvedMethod.hasAnnotation(Pure.class) || rootTransformer.getXmlConfiguration()
							.getConfiguration(method.getDeclaringClass()).getWhitelistMethods().contains(method))) {
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

	public void verify(CtMethod affectedBehavior, boolean allowOwnStateChange) throws CannotCompileException,
			NotFoundException {
		PureBehaviorExpressionEditor editor = new PureBehaviorExpressionEditor(affectedBehavior, rootTransformer, this,
				allowOwnStateChange);
		affectedBehavior.instrument(editor);
		arrayAccessEditor.instrumentArrayAccesses(affectedBehavior);
		if (editor.getPureError() != null) {
			editor.getPureError().insertBefore(affectedBehavior);
		}
		verifyUnpureObjects(affectedBehavior);
	}

	private void verifyUnpureObjects(CtMethod affectedBehavior) throws NotFoundException, CannotCompileException {
		List<NestedExp> unpureObjects = new ArrayList<NestedExp>();
		boolean methodIsStatic = Modifier.isStatic(affectedBehavior.getModifiers());
		if (!methodIsStatic) {
			unpureObjects.add(NestedExp.THIS);
		}
		addParametersToUnpureObjects(affectedBehavior, unpureObjects);
		addFieldsToUnpureObjects(affectedBehavior, unpureObjects, methodIsStatic);
		registerUnpureObjects(affectedBehavior, unpureObjects);
	}

	private void addFieldsToUnpureObjects(CtMethod affectedBehavior, List<NestedExp> unpureObjects,
			boolean methodIsStatic) throws NotFoundException {
		for (CtField field : getAccessibleFields(affectedBehavior)) {
			if (!field.getType().isPrimitive() && (!methodIsStatic || Modifier.isStatic(field.getModifiers()))
					&& !field.hasAnnotation(AllowPureAccess.class)) {
				unpureObjects.add(NestedExp.field(field));
			}
		}
	}

	private void addParametersToUnpureObjects(CtMethod affectedBehavior, List<NestedExp> unpureObjects)
			throws NotFoundException {
		CtClass[] parameterTypes = affectedBehavior.getParameterTypes();
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
			CtClass paramType = parameterTypes[parameterIndex];
			if (!paramType.isPrimitive()) {
				unpureObjects.add(NestedExp.arg(parameterIndex + 1));
			}
		}
	}

	private void registerUnpureObjects(CtBehavior affectedBehavior, List<NestedExp> unpureObjects)
			throws CannotCompileException {
		if (unpureObjects.isEmpty()) {
			return;
		}
		ArrayExp unpureArray = new ArrayExp(Object.class, unpureObjects);
		new StaticCallExp(PureEvaluator.registerUnpure, unpureArray).toStandalone().insertBefore(affectedBehavior);
		new StaticCallExp(PureEvaluator.unregisterUnpure).toStandalone().insertFinally(affectedBehavior);
	}

	private Set<CtField> getAccessibleFields(CtBehavior affectedBehavior) {
		Set<CtField> accessibleFields = new HashSet<CtField>();
		for (CtField field : affectedBehavior.getDeclaringClass().getFields()) {
			if (isAccessibleField(affectedBehavior, field)) {
				accessibleFields.add(field);
			}
		}
		Collections.addAll(accessibleFields, affectedBehavior.getDeclaringClass().getDeclaredFields());
		return accessibleFields;
	}

	private boolean isAccessibleField(CtBehavior affectedBehavior, CtField field) {
		return !Modifier.isPackage(field.getModifiers())
				|| getPackageName(affectedBehavior).equals(getPackageName(field));
	}

	private String getPackageName(CtMember member) {
		return member.getDeclaringClass().getPackageName();
	}

	public void checkUnpureAccess(CtBehavior affectedBehavior) throws CannotCompileException {
		if (!rootTransformer.getXmlConfiguration().isWithinRootPackages(affectedBehavior.getDeclaringClass())) {
			return;
		}
		affectedBehavior.instrument(unpureBehaviorExpressionEditor);
		if (Modifier.isStatic(affectedBehavior.getModifiers())) {
			return;
		}
		StandaloneExp checkUnpureAccessExp = new StaticCallExp(PureEvaluator.checkUnpureAccess, NestedExp.THIS)
				.toStandalone();
		checkUnpureAccessExp.insertBefore(affectedBehavior);
	}

	public void verifyUnchangeable(CtBehavior affectedBehavior, ListOrderedSet<ContractInfo> contracts)
			throws CannotCompileException {
		boolean containsUnchanged = false;
		for (ContractInfo contract : contracts) {
			if (contract.getMethodsContainingUnchanged().contains(affectedBehavior)) {
				containsUnchanged = true;
				break;
			}
		}
		if (containsUnchanged) {
			arrayAccessEditor.instrumentArrayAccesses(affectedBehavior);
		}
	}
}
