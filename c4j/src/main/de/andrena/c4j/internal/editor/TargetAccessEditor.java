package de.andrena.c4j.internal.editor;

import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import org.apache.log4j.Logger;

import de.andrena.c4j.internal.compiler.AssignmentExp;
import de.andrena.c4j.internal.compiler.CastExp;
import de.andrena.c4j.internal.compiler.NestedExp;
import de.andrena.c4j.internal.compiler.StandaloneExp;

public class TargetAccessEditor extends ExprEditor {
	private Map<CtField, CtField> targetFieldMap;
	private Logger logger = Logger.getLogger(getClass());

	public TargetAccessEditor(Map<CtField, CtField> targetFieldMap) {
		this.targetFieldMap = targetFieldMap;
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
		if (targetFieldMap.keySet().contains(field)) {
			CtField weakField = targetFieldMap.get(field);
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
