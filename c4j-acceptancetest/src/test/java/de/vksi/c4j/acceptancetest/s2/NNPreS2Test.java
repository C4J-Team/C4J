package de.vksi.c4j.acceptancetest.s2;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.pre;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.ContractReference;

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

	@ContractReference(MiniStackContract.class)
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
