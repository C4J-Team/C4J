package de.andrena.c4j.acceptancetest.subclasses;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.result;
import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.Condition;
import de.andrena.c4j.Target;

public class TopContract extends Top {

	@Target
	private Top target;

	@Override
	public int pre(String parameter) {
		if (Condition.pre()) {
			assert parameter != null : "parameter must not be null";
		}
		return (Integer) ignored();
	}

	@Override
	public int post(String parameter) {
		if (Condition.post()) {
			assert result(Integer.class) >= 0 : "result >= 0";
		}
		return (Integer) ignored();
	}

	@Override
	public int preAndPost(String parameter) {
		if (Condition.pre()) {
			assert parameter != null : "parameter must not be null";
		}
		if (Condition.post()) {
			assert result(Integer.class) >= 0 : "result >= 0";
		}
		return (Integer) ignored();
	}

	@Override
	public int invariant(String parameter) {
		return (Integer) ignored();
	}

	@ClassInvariant
	public void aIsAlwaysAMultipleOfTwo() {
		assert target.pre("") % 2 == 0 : "a() is a multiple of two";
	}

	@Override
	public int unchanged() {
		if (Condition.post()) {
			assert Condition.unchanged(target.unchanged()) : "unchanged never changes";
		}
		return (Integer) ignored();
	}

	@Override
	public int old() {
		if (Condition.post()) {
			assert result().equals(Condition.old(target.old())) : "old value is preserved";
		}
		return (Integer) ignored();
	}

}
