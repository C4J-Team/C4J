package de.vksi.c4j.acceptancetest.s5b;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.acceptancetest.subclasses.Bottom;

public class NNOldS5bTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenOldValueIsReturned() {
		assertThat(new Bottom(0).old(), is(0));
	}

	@Test
	public void failsWhenDifferentValueIsReturned() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("old value");
		new Bottom(0) {
			@AllowPureAccess
			private int oldDestroyer;

			@Override
			public int old() {
				return value + oldDestroyer++;
			};
		}.old();
	}

}
