package next.internal;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import next.internal.compiler.ArrayExp;
import next.internal.compiler.AssignmentExp;
import next.internal.compiler.CastExp;
import next.internal.compiler.NestedExp;
import next.internal.compiler.StaticCallExp;
import next.internal.compiler.ValueExp;

import org.apache.log4j.Logger;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	private CtClass targetClass;

	public ContractMethodExpressionEditor(CtClass targetClass) {
		this.targetClass = targetClass;
	}

	@Override
	public void edit(FieldAccess fieldAccess) {
		try {
			if (!fieldAccess.isStatic() && fieldAccess.getField().getDeclaringClass().equals(targetClass)) {
				if (fieldAccess.isWriter()) {
					throw new TransformationException("illegal write access on field '" + fieldAccess.getFieldName()
							+ "'.");
				}
				CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.fieldAccess, new ValueExp(
						fieldAccess.getFieldName())));
				AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
				fieldAccess.replace(assignment.toStandalone().getCode());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void edit(MethodCall methodCall) {
		try {
			CtMethod method = methodCall.getMethod();
			if (method.getDeclaringClass().equals(targetClass)) {
				CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.methodCall, new ValueExp(
						methodCall.getMethodName()), ArrayExp.forParamTypes(method), ArrayExp.forArgs(method)));
				AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
				methodCall.replace(assignment.toStandalone().getCode());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
