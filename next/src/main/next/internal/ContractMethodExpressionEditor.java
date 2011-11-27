package next.internal;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import next.Condition;
import next.internal.compiler.IfExp;
import next.internal.compiler.StandaloneExp;
import next.internal.compiler.StaticCall;
import next.internal.compiler.StaticCallExp;

import org.apache.log4j.Logger;

public class ContractMethodExpressionEditor extends ExprEditor {
	private Logger logger = Logger.getLogger(getClass());

	@Override
	public void edit(FieldAccess fieldAccess) {
		// TODO
	}

	@Override
	public void edit(MethodCall methodCall) {
		logger.info("encountered methodCall " + methodCall.getMethodName());
		try {
			if (methodCall.getClassName().equals(Condition.class.getName())) {
				if (methodCall.getMethodName().equals("pre")) {
					logger.info("found pre in method " + methodCall.where().getLongName());
					methodCall.replace(getCondition(methodCall, Evaluator.isBefore).getCode());
				} else if (methodCall.getMethodName().equals("post") || methodCall.getMethodName().equals("result")) {
					logger.info("found post in method " + methodCall.where().getLongName());
					methodCall.replace(getCondition(methodCall, Evaluator.isAfter).getCode());
				}
			}
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected IfExp getCondition(MethodCall methodCall, StaticCall condition) throws CannotCompileException {
		IfExp preActive = new IfExp(new StaticCallExp(condition));
		preActive.addIfBody(StandaloneExp.proceed);
		return preActive;
	}

}
