package de.andrena.c4j.acceptancetest.s5b;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.acceptancetest.subclasses.Bottom;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class NNPostS5bTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenPostConditionIsSatisfied() {
		assertThat(new Bottom(0).post(""), is(0));
	}
	
	@Test
	public void failsWhenPostConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("result >= 0");
		new Bottom(-2).post("");
	}
}
