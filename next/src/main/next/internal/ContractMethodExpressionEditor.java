package next.internal;

import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

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
	}

}
