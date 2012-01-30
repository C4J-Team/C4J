package de.andrena.next.systemtest.pure;

import org.junit.Test;

import de.andrena.next.Pure;

public class PureOnlyForOwnMethodsFieldsAndParametersSystemTest {

	@Test
	public void testPure() {
		new TargetClass().pureMethod("paramValue");
	}

	public static class TargetClass {
		private String field = "fieldValue";

		@Pure
		public void pureMethod(String param) {
			StringBuilder builder = new StringBuilder();
			builder.append(field);
			builder.append(" - ");
			builder.append(param);
		}
	}

	// TODO: test params, fields, own methods, static, etc.

}
