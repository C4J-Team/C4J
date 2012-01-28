package de.andrena.next.acceptancetest.subclasses;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.result;
import de.andrena.next.ClassInvariant;
import de.andrena.next.Condition;
import de.andrena.next.Pure;

public class TopContract extends Top {

	@Override
	public int pre(String parameter) {
		if (Condition.pre()) {
			assert parameter != null : "parameter must not be null";
		}
		return ignored();
	}

	@Override
	public int post(String parameter) {
		if (Condition.post()) {
			assert result(Integer.class) >= 0 : "result >= 0";
		}
		return ignored();
	}

	@Override
	public int preAndPost(String parameter) {
		if (Condition.pre()) {
			assert parameter != null : "parameter must not be null";
		}
		if (Condition.post()) {
			assert result(Integer.class) >= 0 : "result >= 0";
		}
		return ignored();
	}

	@Override
	public int invariant(String parameter) {
		return ignored();
	}

	@ClassInvariant
	public void aIsAlwaysAMultipleOfTwo() {
		assert target().pre("") % 2 == 0 : "a() is a multiple of two";
	}

	@Override
	public int unchanged() {
		if (Condition.post()) {
			assert Condition.unchanged(target().unchanged()) : "unchanged never changes";
		}
		return ignored();
	}

	@Override
	public int old() {
		if (Condition.post()) {
			assert result().equals(Condition.old(target().old())) : "old value is preserved";
		}
		return ignored();
	}

	@Pure
	private Top target() {
		return Condition.target();
	}

}
