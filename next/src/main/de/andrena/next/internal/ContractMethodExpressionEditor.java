package de.andrena.next.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import org.apache.log4j.Logger;

import de.andrena.next.Condition;
import de.andrena.next.Condition.PostCondition;
import de.andrena.next.Condition.PreCondition;
import de.andrena.next.internal.ContractRegistry.ContractInfo;
import de.andrena.next.internal.compiler.ArrayExp;
import de.andrena.next.internal.compiler.AssignmentExp;
import de.andrena.next.internal.compiler.CastExp;
import de.andrena.next.internal.compiler.IfExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.compiler.ValueExp;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());
	String lastMethodCall;
	String lastFieldAccess;
	private List<StaticCallExp> storeExpressions = new ArrayList<StaticCallExp>();
	private Set<CtClass> nestedInnerClasses = new HashSet<CtClass>();
	private ClassPool pool;
	private ContractInfo contract;

	public List<StaticCallExp> getStoreExpressions() {
		return storeExpressions;
	}

	public ContractMethodExpressionEditor(ContractInfo contract, ClassPool pool) throws NotFoundException {
		this.contract = contract;
		this.pool = pool;
	}

	public Set<CtClass> getNestedInnerClasses() {
		return nestedInnerClasses;
	}

	@Override
	public void edit(FieldAccess fieldAccess) throws CannotCompileException {
		try {
			editFieldAccess(fieldAccess);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	void editFieldAccess(FieldAccess fieldAccess) throws NotFoundException, CannotCompileException {
		lastFieldAccess = fieldAccess.getFieldName();
		lastMethodCall = null;
		logger.info("last field access: " + fieldAccess.getFieldName());
		if (!fieldAccess.isStatic() && fieldAccess.getField().getDeclaringClass().equals(contract.getTargetClass())) {
			if (fieldAccess.isWriter()) {
				throw new TransformationException("illegal write access on field '" + fieldAccess.getFieldName() + "'.");
			}
			CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.fieldAccess, new ValueExp(
					fieldAccess.getFieldName())));
			AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
			fieldAccess.replace(assignment.toStandalone().getCode());
		}
	}

	@Override
	public void edit(NewExpr newExpr) throws CannotCompileException {
		try {
			editNewExpression(newExpr);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	void editNewExpression(NewExpr newExpr) throws NotFoundException, CannotCompileException {
		logger.info("NewExpr2 found: " + newExpr.getClassName());
		CtClass exprClass = pool.get(newExpr.getClassName());
		if (exprClass.getInterfaces().length != 1) {
			return;
		}
		CtClass interfaze = exprClass.getInterfaces()[0];
		IfExp replacementExp = null;
		if (interfaze.getName().equals(PreCondition.class.getName())) {
			logger.info("PreCondition found, replacing...");
			replacementExp = new IfExp(new StaticCallExp(Evaluator.isBefore));
		} else if (interfaze.getName().equals(PostCondition.class.getName())) {
			logger.info("PostCondition found, replacing...");
			replacementExp = new IfExp(new StaticCallExp(Evaluator.isAfter));
		}
		if (replacementExp != null) {
			contract.addInnerContractClass(exprClass);
			nestedInnerClasses.add(exprClass);
			replacementExp.addIfBody(StandaloneExp.proceed);
			newExpr.replace(replacementExp.getCode());
		}
	}

	@Override
	public void edit(MethodCall methodCall) throws CannotCompileException {
		try {
			editMethodCall(methodCall);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		}
	}

	void editMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		if (method.getDeclaringClass().equals(contract.getTargetClass())
				|| method.getDeclaringClass().equals(contract.getContractClass())) {
			handleTargetMethodCall(methodCall);
		} else if (method.getDeclaringClass().getName().equals(Condition.class.getName())
				&& method.getName().equals("old")) {
			handleOldMethodCall(methodCall);
		}
	}

	void handleOldMethodCall(MethodCall methodCall) throws CannotCompileException {
		StaticCallExp oldCall = null;
		if (lastFieldAccess != null) {
			logger.info("storing field access to " + lastFieldAccess);
			storeExpressions.add(new StaticCallExp(Evaluator.storeFieldAccess, new ValueExp(lastFieldAccess)));
			oldCall = new StaticCallExp(Evaluator.oldFieldAccess, new ValueExp(lastFieldAccess));
		} else if (lastMethodCall != null) {
			logger.info("storing method call to " + lastMethodCall);
			storeExpressions.add(new StaticCallExp(Evaluator.storeMethodCall, new ValueExp(lastMethodCall)));
			oldCall = new StaticCallExp(Evaluator.oldMethodCall, new ValueExp(lastMethodCall));
		}
		if (oldCall != null) {
			AssignmentExp assignmentExp = new AssignmentExp(NestedExp.RETURN_VALUE, oldCall);
			methodCall.replace(assignmentExp.toStandalone().getCode());
		}
	}

	void handleTargetMethodCall(MethodCall methodCall) throws NotFoundException, CannotCompileException {
		CtMethod method = methodCall.getMethod();
		lastMethodCall = methodCall.getMethodName();
		lastFieldAccess = null;
		logger.info("last method call: " + lastMethodCall);
		logger.info("replacing call to " + methodCall.getClassName() + "." + methodCall.getMethodName());
		CastExp replacementCall = CastExp.forReturnType(new StaticCallExp(Evaluator.methodCall, new ValueExp(methodCall
				.getMethodName()), ArrayExp.forParamTypes(method), ArrayExp.forArgs(method)));
		AssignmentExp assignment = new AssignmentExp(NestedExp.RETURN_VALUE, replacementCall);
		String code = assignment.toStandalone().getCode();
		logger.info("replacement code: " + code);
		methodCall.replace(code);
	}
}
