package de.andrena.next.acceptancetest;

import static de.andrena.next.Condition.ignored;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.Contract;
import de.andrena.next.Condition.PreCondition;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNPreS4Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private MiniStack<String> dummy;

	@Before
	public void before() {
		dummy = new MiniStack<String>();
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
	public static class MiniStack<T> {
		public T get(int index) {
			return null;
		}
	}

	public static class MiniStackContract<T> extends MiniStack<T> {
		@Override
		public T get(final int index) {
			new PreCondition() {
				{
					assert index > 0 : "index greater 0";
				}
			};
			return ignored();
		}
	}

}
