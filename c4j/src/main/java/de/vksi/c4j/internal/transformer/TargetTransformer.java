package de.vksi.c4j.internal.transformer;

import static de.vksi.c4j.internal.util.ReflectionHelper.getSimpleName;

import java.lang.ref.WeakReference;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import de.vksi.c4j.Target;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.ConstructorExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.editor.TargetAccessEditor;
import de.vksi.c4j.internal.evaluator.Evaluator;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;
import de.vksi.c4j.internal.util.Pair;

public class TargetTransformer extends AbstractContractClassTransformer {

	public static final String TARGET_FIELD_NAME = "target$";
	private static final String EXPECTED_TARGET_FIELD_NAME = "target";
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	private static class WeakFieldMapping extends Pair<CtField, CtField> {
		public WeakFieldMapping(CtField targetField, CtField weakField) {
			super(targetField, weakField);
		}
	}

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		WeakFieldMapping targetField = createWeakField(contractClass);
		if (targetField == null) {
			return;
		}
		if (!contractInfo.getTargetClass().subtypeOf(targetField.getFirst().getType())) {
			logger.error("Target reference " + getSimpleName(targetField.getFirst()) + " has incompatible type.");
			return;
		}
		if (!contractInfo.getTargetClass().equals(targetField.getFirst().getType())) {
			logger.warn("Target reference " + getSimpleName(targetField.getFirst())
					+ " has weaker type than the target type would allow.");
		}
		TargetAccessEditor targetAccessEditor = new TargetAccessEditor(targetField);
		for (CtBehavior contractBehavior : contractClass.getDeclaredBehaviors()) {
			if (logger.isTraceEnabled()) {
				logger.trace("instrumenting " + contractBehavior.getLongName());
			}
			contractBehavior.instrument(targetAccessEditor);
		}
		initWeakField(contractClass, targetField.getSecond());
	}

	private void initWeakField(CtClass contractClass, CtField weakField) throws NotFoundException,
			CannotCompileException {
		CtConstructor defaultConstructor = contractClass.getDeclaredConstructor(new CtClass[0]);
		ConstructorExp weakConstructorCall = new ConstructorExp(WeakReference.class, new StaticCallExp(
				Evaluator.getCurrentTarget));
		new AssignmentExp(NestedExp.field(weakField), weakConstructorCall).insertBefore(defaultConstructor);
	}

	private WeakFieldMapping createWeakField(CtClass contractClass) throws NotFoundException, CannotCompileException {
		CtClass weakReferenceClass = rootTransformer.getPool().get(WeakReference.class.getName());
		CtField targetField = getTargetField(contractClass);
		if (targetField == null) {
			return null;
		}
		CtField weakTargetField = new CtField(weakReferenceClass, TARGET_FIELD_NAME, contractClass);
		contractClass.addField(weakTargetField);
		return new WeakFieldMapping(targetField, weakTargetField);
	}

	private CtField getTargetField(CtClass contractClass) {
		CtField targetField = null;
		for (CtField field : contractClass.getDeclaredFields()) {
			if (field.hasAnnotation(Target.class)) {
				if (targetField != null) {
					logger.error("Contract " + contractClass.getSimpleName()
							+ " has multiple fields annotated with @Target, only the first one is being set.");
				} else {
					targetField = field;
				}
			} else if (field.getName().equals(EXPECTED_TARGET_FIELD_NAME)) {
				logger.warn("Field " + getSimpleName(field) + " is possibly missing annotation @Target.");
			}
		}
		return targetField;
	}

}
