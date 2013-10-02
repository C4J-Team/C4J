package de.vksi.c4j.internal.transformer.contract;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getSimpleName;

import java.lang.ref.WeakReference;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.Target;
import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.ConstructorExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.runtime.Evaluator;
import de.vksi.c4j.internal.transformer.editor.TargetAccessEditor;
import de.vksi.c4j.internal.transformer.util.ContractClassMemberHelper;
import de.vksi.c4j.internal.types.Pair;

public class TargetTransformer extends AbstractContractClassTransformer {
	private static final Logger LOGGER = Logger.getLogger(TargetTransformer.class);

	private static final String EXPECTED_TARGET_FIELD_NAME = "target";

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
			LOGGER.error("Target reference " + getSimpleName(targetField.getFirst()) + " has incompatible type.");
			return;
		}
		if (!contractInfo.getTargetClass().equals(targetField.getFirst().getType())) {
			LOGGER.warn("Target reference " + getSimpleName(targetField.getFirst())
					+ " has weaker type than the target type would allow.");
		}
		TargetAccessEditor targetAccessEditor = new TargetAccessEditor(targetField);
		for (CtBehavior contractBehavior : contractClass.getDeclaredBehaviors()) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("instrumenting " + contractBehavior.getLongName());
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
		CtField targetField = getTargetField(contractClass);
		if (targetField == null) {
			return null;
		}
		CtField weakTargetField = ContractClassMemberHelper.createWeakTargetField(contractClass);
		contractClass.addField(weakTargetField);
		return new WeakFieldMapping(targetField, weakTargetField);
	}

	private CtField getTargetField(CtClass contractClass) {
		CtField targetField = null;
		for (CtField field : contractClass.getDeclaredFields()) {
			if (field.hasAnnotation(Target.class)) {
				if (targetField != null) {
					LOGGER.error("Contract " + contractClass.getSimpleName()
							+ " has multiple fields annotated with @Target, only the first one is being set.");
				} else {
					targetField = field;
				}
			} else if (field.getName().equals(EXPECTED_TARGET_FIELD_NAME)) {
				LOGGER.warn("Field " + getSimpleName(field) + " is possibly missing annotation @Target.");
			}
		}
		return targetField;
	}

}
