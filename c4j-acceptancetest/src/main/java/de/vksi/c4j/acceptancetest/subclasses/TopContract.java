package de.vksi.c4j.acceptancetest.subclasses;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.result;
import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.Condition;
import de.vksi.c4j.Target;

public class TopContract extends Top {

	@Target
	private Top target;

	@Override
	public int pre(String parameter) {
		if (Condition.preCondition()) {
			assert parameter != null : "parameter must not be null";
		}
		return (Integer) ignored();
	}

	@Override
	public int post(String parameter) {
		if (Condition.postCondition()) {
			assert result(Integer.class) >= 0 : "result >= 0";
		}
		return (Integer) ignored();
	}

	@Override
	public int preAndPost(String parameter) {
		if (Condition.preCondition()) {
			assert parameter != null : "parameter must not be null";
		}
		if (Condition.postCondition()) {
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
		if (Condition.postCondition()) {
			assert Condition.unchanged(target.unchanged()) : "unchanged never changes";
		}
		return (Integer) ignored();
	}

	@Override
	public int old() {
		if (Condition.postCondition()) {
			assert result().equals(Condition.old(target.old())) : "old value is preserved";
		}
		return (Integer) ignored();
	}

}
