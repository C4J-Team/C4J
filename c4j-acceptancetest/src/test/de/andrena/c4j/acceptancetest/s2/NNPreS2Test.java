package de.andrena.c4j.acceptancetest.s2;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.next.Contract;

public class NNPreS2Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private MiniStack<String> dummy;

	@Before
	public void before() {
		dummy = new MiniStringStack();
	}

	@Test
	public void testPreCondition() {
		dummy.get(3);
	}

	@Test
	public void testPreConditionFails() {
		thrown.expect(AssertionError.class);
		dummy.get(0);
	}

	@Contract(MiniStackContract.class)
	public static interface MiniStack<T> {
		public T get(int index);
	}

	public static class MiniStringStack implements MiniStack<String> {
		@Override
		public String get(int index) {
			return null;
		}
	}

	public static class MiniStackContract<T> implements MiniStack<T> {
		@Override
		public T get(final int index) {
			if (pre()) {
				assert index > 0 : "index greater 0";
			}
			return ignored();
		}
	}

}
