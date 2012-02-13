package de.andrena.next.acceptancetest.object;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.result;
import de.andrena.next.Target;

public class ObjectSpecContract implements ObjectSpec {

	@Target
	private ObjectSpec target;

	private Object z;

	@Override
	public boolean equals(Object obj) {
		if (post()) {
			boolean result = result(Boolean.class);
			Object x = target;
			Object y = obj;

			if (z == null) {
				z = obj;
			}

			if (obj == null) {
				assert result == false : "if obj == null then false";
			} else {
				assert x.equals(x) : "is reflexive";

				assert x.equals(y) == y.equals(x) : "is symmetric";

				if (x.equals(y) && y.equals(z)) {
					assert x.equals(z) : "is transitive";
				}

				assert x.equals(y) == x.equals(y) : "is consistent with equals";

				if (x.equals(y)) {
					assert x.hashCode() == y.hashCode() : "is consistent with hashCode";
				}
			}
		}

		return ignored();
	}

	@Override
	public String toString() {
		if (post()) {
			String result = result(String.class);
			assert result != null : "result not null";
		}
		return ignored();
	}

}