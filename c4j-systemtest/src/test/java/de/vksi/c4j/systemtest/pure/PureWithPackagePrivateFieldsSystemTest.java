package de.vksi.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.systemtest.pure.otherpackage.SuperClass;

public class PureWithPackagePrivateFieldsSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPureWithPackagePrivateFieldAccess() {
		new TargetClass().method();
	}

	public static class TargetClass extends SuperClass {
		@Pure
		public void method() {
		}
	}

}
