package de.andrena.next.acceptancetest.s5a;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.acceptancetest.subinterfaces.VeryBottom;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNOldS5aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenOldValueIsReturned() {
		assertThat(new VeryBottom(0).old(), is(0));
	}
	
	@Test
	public void failsWhenDifferentValueIsReturned() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("old value");
		new VeryBottom(0) {
			public int old() {
				return value++;
			};
		}.old();
	}

}