package de.vksi.c4j.internal.editor;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.compiler.AssignmentExp;
import de.vksi.c4j.internal.compiler.CastExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.util.Pair;

public class TargetAccessEditor extends ExprEditor {
	private Pair<CtField, CtField> targetField;
	private Logger logger = Logger.getLogger(getClass());

	public TargetAccessEditor(Pair<CtField, CtField> targetField) {
		this.targetField = targetField;
	}

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	private void editFieldAccess(FieldAccess fieldAccess) throws NotFoundException, CannotCompileException {
		CtField field = fieldAccess.getField();
		if (targetField.getFirst().equals(field)) {
			CtField weakField = targetField.getSecond();
			StandaloneExp replacementExp;
			if (fieldAccess.isReader()) {
				NestedExp getTargetExp = new CastExp(field.getType(), NestedExp.field(weakField).appendCall("get"));
				replacementExp = new AssignmentExp(NestedExp.RETURN_VALUE, getTargetExp).toStandalone();
			} else {
				replacementExp = NestedExp.field(weakField).appendCall("set", NestedExp.arg(1)).toStandalone();
			}
			if (logger.isTraceEnabled()) {
				logger.trace("target field access replacement: " + replacementExp.getCode());
			}
			replacementExp.replace(fieldAccess);
		}
	}

}
