package de.vksi.c4j.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import de.vksi.c4j.internal.types.Pair;

public class TryExp extends StandaloneExp {
	private StandaloneExp tryExp;
	private StandaloneExp finallyExp;
	private List<CatchClause> catchClauses = new ArrayList<CatchClause>();

	private static class CatchClause extends Pair<Class<?>, StandaloneExp> {
		public CatchClause(Class<?> catchClass, StandaloneExp catchExp) {
			super(catchClass, catchExp);
		}
	}

	public TryExp(StandaloneExp body) {
		this.tryExp = body;
	}

	public void addFinally(StandaloneExp finallyExp) {
		this.finallyExp = finallyExp;
	}

	public void addCatch(Class<?> catchClass, StandaloneExp catchExp) {
		catchClauses.add(new CatchClause(catchClass, catchExp));
	}

	@Override
	public String getCode() {
		StringBuilder code = new StringBuilder("\n").append("try {").append(tryExp.getCode()).append("\n").append("}");
		int i = 1;
		for (CatchClause catchClause : catchClauses) {
			code.append(" catch (").append(catchClause.getFirst().getName()).append(" e").append(i).append(") {")
					.append(catchClause.getSecond().getCode()).append("\n").append("}");
			i++;
		}
		if (finallyExp != null) {
			code.append(" finally {").append(finallyExp.getCode()).append("\n").append("}");
		}
		return code.toString();
	}

	public NestedExp getCatchClauseVar(int num) {
		return NestedExp.var("e" + num);
	}

}
