package singlepackage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testPureMethod() {
		target.pureMethod();
	}

	@Test(expected = AssertionError.class)
	public void testPureMethodWritingField() {
		target.unpureMethodWritingField();
	}

	public static class TargetClass {
		protected String field = "sample";

		@Pure
		public void pureMethod() {
		}

		@Pure
		public void unpureMethodWritingField() {
			field = "illegal";
		}

	}
}
