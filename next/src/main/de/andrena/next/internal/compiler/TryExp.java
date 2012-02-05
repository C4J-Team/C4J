package de.andrena.next.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import de.andrena.next.internal.util.Pair;

public class TryExp extends StandaloneExp {
	private StandaloneExp tryExp;
	private StandaloneExp finallyExp;
	private List<Pair<Class<?>, StandaloneExp>> catchClauses = new ArrayList<Pair<Class<?>, StandaloneExp>>();

	public TryExp(StandaloneExp body) {
		this.tryExp = body;
	}

	public void addFinally(StandaloneExp finallyExp) {
		this.finallyExp = finallyExp;
	}

	public void addCatch(Class<?> catchClass, StandaloneExp catchExp) {
		catchClauses.add(new Pair<Class<?>, StandaloneExp>(catchClass, catchExp));
	}

	@Override
	public String getCode() {
		String code = "\n" + "try {" + tryExp.getCode() + "\n" + "}";
		int i = 1;
		for (Pair<Class<?>, StandaloneExp> catchClause : catchClauses) {
			code += " catch (" + catchClause.getFirst().getName() + " e" + i + ") {"
					+ catchClause.getSecond().getCode() + "\n" + "}";
			i++;
		}
		if (finallyExp != null) {
			code += " finally {" + finallyExp.getCode() + "\n" + "}";
		}
		return code;
	}

	public NestedExp getCatchClauseVar(int num) {
		return NestedExp.var("e" + num);
	}

}
