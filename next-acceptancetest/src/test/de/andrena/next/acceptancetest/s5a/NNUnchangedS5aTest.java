package de.andrena.next.acceptancetest.s5a;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.AllowPureAccess;
import de.andrena.next.acceptancetest.subinterfaces.VeryBottom;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNUnchangedS5aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenUnchanged() {
		assertThat(new VeryBottom(0).unchanged(), is(0));
	}

	@Test
	public void failsWhenClassInvariantConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("unchanged");
		new VeryBottom(0) {
			@AllowPureAccess
			private int unchangedDestroyer;

			@Override
			public int unchanged() {
				return value + unchangedDestroyer++;
			};
		}.unchanged();
	}

}
