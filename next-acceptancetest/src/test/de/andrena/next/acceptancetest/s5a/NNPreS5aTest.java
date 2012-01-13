package de.andrena.next.acceptancetest.s5a;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.acceptancetest.subinterfaces.VeryBottom;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNPreS5aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenPreConditionIsSatisfied() {
		assertThat(new VeryBottom(42).a(""), is(42));
	}
	
	@Test
	public void failsWhenPreConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("must not be null");
		new VeryBottom(42).a(null);
	}
}
