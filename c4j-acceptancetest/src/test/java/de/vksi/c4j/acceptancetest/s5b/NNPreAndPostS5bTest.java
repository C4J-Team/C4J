package de.vksi.c4j.acceptancetest.s5b;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.acceptancetest.subclasses.Bottom;

public class NNPreAndPostS5bTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenPreConditionIsSatisfied() {
		assertThat(new Bottom(42).preAndPost(""), is(42));
	}
	
	@Test
	public void failsWhenPreConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("must not be null");
		new Bottom(42).preAndPost(null);
	}

	@Test
	public void returnsValueWhenPostConditionIsSatisfied() {
		assertThat(new Bottom(0).preAndPost(""), is(0));
	}
	
	@Test
	public void failsWhenPostConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("result >= 0");
		new Bottom(-2).preAndPost("");
	}
}
