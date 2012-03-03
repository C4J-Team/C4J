package de.andrena.c4j.acceptancetest.s5a;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.acceptancetest.subinterfaces.VeryBottom;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class NNPreAndPostS5aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenPreConditionIsSatisfied() {
		assertThat(new VeryBottom(42).preAndPost(""), is(42));
	}
	
	@Test
	public void failsWhenPreConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("must not be null");
		new VeryBottom(42).preAndPost(null);
	}

	@Test
	public void returnsValueWhenPostConditionIsSatisfied() {
		assertThat(new VeryBottom(0).preAndPost(""), is(0));
	}
	
	@Test
	public void failsWhenPostConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("result >= 0");
		new VeryBottom(-2).preAndPost("");
	}
}
