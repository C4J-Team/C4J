package next.internal;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import next.Condition;
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
	private String lastMethodCall;
	private String lastFieldAccess;
	private List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
	private CtClass contractClass;

	public List<StaticCallExp> getStoreExpressions() {
		return storeExpressions;
	}

	public ContractMethodExpressionEditor(CtClass contractClass) throws NotFoundException {
		this.contractClass = contractClass;
		this.targetClass = contractClass.getSuperclass();
	}

	@Override
	public void edit(FieldAccess fieldAccess) {
		try {
			lastFieldAccess = fieldAccess.getFieldName();
			lastMethodCall = null;
			logger.info("last field access: " + fieldAccess.getFieldName());
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
			if (method.getDeclaringClass().equals(targetClass) || method.getDeclaringClass().equals(contractClass)) {
				lastMethodCall = methodCall.getMethodName();
				lastFieldAccess = null;
				logger.info("last method call: " + lastMethodCall);
				logger.info("replacing call to " + methodCall.getClassName() + "." + methodCall.getMethodName());
				CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.methodCall, new ValueExp(
						methodCall.getMethodName()), ArrayExp.forParamTypes(method), ArrayExp.forArgs(method)));
				AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
				String code = assignment.toStandalone().getCode();
				logger.info("replacement code: " + code);
				methodCall.replace(code);
			} else if (method.getDeclaringClass().getName().equals(Condition.class.getName())
					&& method.getName().equals("old")) {
				if (lastFieldAccess != null) {
					logger.info("storing field access to " + lastFieldAccess);
					storeExpressions.add(new StaticCallExp(Evaluator.storeFieldAccess, new ValueExp(lastFieldAccess)));
					StaticCallExp oldCall = new StaticCallExp(Evaluator.oldFieldAccess, new ValueExp(lastFieldAccess));
					AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
					methodCall.replace(assignmentExp.toStandalone().getCode());
				} else if (lastMethodCall != null) {
					logger.info("storing method call to " + lastMethodCall);
					storeExpressions.add(new StaticCallExp(Evaluator.storeMethodCall, new ValueExp(lastMethodCall)));
					StaticCallExp oldCall = new StaticCallExp(Evaluator.oldMethodCall, new ValueExp(lastMethodCall));
					AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
					methodCall.replace(assignmentExp.toStandalone().getCode());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
