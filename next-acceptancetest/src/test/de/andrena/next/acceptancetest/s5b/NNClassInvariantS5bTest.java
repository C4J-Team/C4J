package de.andrena.next.acceptancetest.s5b;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.acceptancetest.subclasses.Bottom;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNClassInvariantS5bTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenClassInvariantIsSatisfied() {
		assertThat(new Bottom(2).invariant(""), is(2));
	}
	
	@Test
	public void failsWhenClassInvariantConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("multiple of two");
		new Bottom(3).invariant("");
	}
}
